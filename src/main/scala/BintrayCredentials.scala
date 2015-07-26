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

  def sbtCredentials(allCredentials: Seq[Credentials], host: String) = {
    Credentials.forHost(allCredentials, host).map { c =>
      (c.userName, c.passwd)
    }
  }

}
