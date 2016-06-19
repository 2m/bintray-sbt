initialize := {
  System.setProperty("bintray.user", "username")
  System.setProperty("bintray.pass", "password")
}

TaskKey[Unit]("check") := {
  val whoami = bintrayWhoami.value
  if (whoami != "username") error(s"unexpected whoami output: $whoami")
  ()
}
