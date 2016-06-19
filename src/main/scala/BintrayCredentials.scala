package bintray

import sbt.IO
import java.io.File
import sbt.Credentials

case class BintrayCredentials(
  user: String, password: String) {
  override def toString = s"BintrayCredentials($user, ${"x"*password.size})"
}

object BintrayCredentials {

  /** bintray api */
  object api {
    def toDirect(bc: BintrayCredentials) =
      sbt.Credentials(Realm, Host, bc.user, bc.password)
    val Host = "api.bintray.com"
    val Realm = "Bintray API Realm"
  }

  /** sonatype oss (for mvn central sync) */
  object sonatype {
    val Host = "oss.sonatype.org"
    val Realm = "Sonatype Nexus Repository Manager"
  }

  def cachedCredentials(key: String) = {
    val cached = Cache.getMulti(s"$key.user", s"$key.pass")
    (cached(s"$key.user"), cached(s"$key.pass")) match {
      case (Some(user), Some(pass)) => Some((user, pass))
      case _ => None
    }
  }

  def propsCredentials(key: String) = {
    for {
      name <- sys.props.get(s"$key.user")
      pass <- sys.props.get(s"$key.pass")
    } yield (name, pass)
  }

  def envCredentials(key: String) = {
    for {
      name <- sys.env.get(s"${key.toUpperCase}_USER")
      pass <- sys.env.get(s"${key.toUpperCase}_PASS")
    } yield (name, pass)
  }

  def sbtCredentials(allCredentials: Seq[Credentials], host: String) = {
    Credentials.forHost(allCredentials, host).map { c =>
      (c.userName, c.passwd)
    }
  }

}
