package com.rolandmit.so2aircodex

/** Hex commands used to control the scooter. */
object Commands {
    const val SECRET = "5A"
    const val HANDSHAKE = "D707A05A00012"
    const val UNLOCK = "D707A25A00003"
    const val LOCK = "D707A25A00014"
    const val ECO = "D707A45A00005"
    const val NORMAL = "D707A45A00016"
    const val SPORT = "D707A25A00027"
    const val SPEED_20_PREF = "D707A90000C878"
    const val SPEED_20_ALT = "D707A90000C868"
    const val SPEED_27 = "D707A900010EAF"
}
