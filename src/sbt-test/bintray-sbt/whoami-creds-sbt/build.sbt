credentials += Credentials(
  "Bintray API Realm",
  "api.bintray.com",
  "username",
  "password"
)

TaskKey[Unit]("check") := {
  val whoami = bintrayWhoami.value
  if (whoami != "username") error(s"unexpected whoami output: $whoami")
  ()
}
