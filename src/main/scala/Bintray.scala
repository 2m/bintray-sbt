package bintray

import sbt._
import bintry.{ Licenses, Client }
import scala.util.Try
import scala.collection.concurrent.TrieMap

object Bintray {
  val defaultMavenRepository = "maven"
  // http://www.scala-sbt.org/0.13/docs/Bintray-For-Plugins.html
  val defaultSbtPluginRepository = "sbt-plugins"

  def whoami(creds: Option[BintrayCredentials], log: Logger): String =
    {
      val is = creds match {
        case None =>
          "nobody"
        case Some(BintrayCredentials(user, _)) =>
          user
      }
      log.info(is)
      is
    }

  private[bintray] def ensureLicenses(licenses: Seq[(String, URL)], omit: Boolean): Unit =
    {
      val acceptable = Licenses.Names.toSeq.sorted.mkString(", ")
      if (!omit) {
        if (licenses.isEmpty) sys.error(
          s"you must define at least one license for this project. Please choose one or more of\n $acceptable")
        if (!licenses.forall { case (name, _) => Licenses.Names.contains(name) }) sys.error(
          s"One or more of the defined licenses were not among the following allowed licenses\n $acceptable")
      }
    }

  def withRepo[A](creds: Option[BintrayCredentials], org: Option[String], repoName: String)
    (f: BintrayRepo => A): Option[A] =
    ensuredCredentials(creds) map { cred =>
      val repo = cachedRepo(cred, org, repoName)
      f(repo)
    }

  private val repoCache: TrieMap[(BintrayCredentials, Option[String], String), BintrayRepo] = TrieMap()
  def cachedRepo(credential: BintrayCredentials, org: Option[String], repoName: String): BintrayRepo =
    repoCache.getOrElseUpdate((credential, org, repoName), BintrayRepo(credential, org, repoName))

  private[bintray] def ensuredCredentials(
    creds: Option[BintrayCredentials]): Option[BintrayCredentials] =
    creds match {
      case None =>
        println("No bintray credentials found in configured sbt credentials.")
        println("Some bintray features depend on this.")
        None
      case creds => creds
    }

  private[bintray] def buildResolvers(creds: Option[BintrayCredentials], org: Option[String], repoName: String): Seq[Resolver] =
    creds.fold(Seq.empty[Resolver]) {
      case BintrayCredentials(user, _) => Seq(Resolver.bintrayRepo(org.getOrElse(user), repoName))
    }

  private[bintray] object await {
    import scala.concurrent.{ Await, Future }
    import scala.concurrent.duration.Duration

    def result[T](f: => Future[T]) = Await.result(f, Duration.Inf)
    def ready[T](f: => Future[T]) = Await.ready(f, Duration.Inf)
  }

  def resolveVcsUrl: Try[Option[String]] =
    Try {
      val pushes =
        sbt.Process("git" :: "remote" :: "-v" :: Nil).!!.split("\n")
         .map {
           _.split("""\s+""") match {
             case Array(name, url, "(push)") =>
               Some((name, url))
             case e =>
               None
           }
         }.flatten
      pushes
        .find { case (name, _) => "origin" == name }
        .orElse(pushes.headOption)
        .map { case (_, url) => url }
    }
}
