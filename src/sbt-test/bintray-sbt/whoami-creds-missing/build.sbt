TaskKey[Unit]("check") := {
  val whoami = bintrayWhoami.value
  if (whoami != "nobody") error(s"unexpected whoami output: $whoami")
  ()
}
