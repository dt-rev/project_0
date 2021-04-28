package com.revature.proj0.cli

import scala.util.matching.Regex
import scala.io.StdIn
import util.control.Breaks._
import com.revature.proj0.utils.ConnectionUtil
import com.revature.proj0.dao.Dao

class Cli {
    val commandArgPattern: Regex = "(\\w+)\\s*(.*)".r

    val db = "jdbc:postgresql://localhost:5432/project0"
    val user = "postgres"
    val pass = "postgres"

    def menu(): Unit = {
        printWelcome()
        var continueMenuLoop = true
        do {
            printMenuOptions()

            var input = StdIn.readLine()
            input match {
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("g") => {
                    println("\nWhat would you like to sort by?")
                    println("[T] Title")
                    println("[D] Developer")
                    println("[P] Publisher")
                    println("[E] ESRB Rating")
                    println("[C] Console/Platform")
                    println("[R] Release Date")

                    var continueGamesLoop = false
                    var orderBy = "title"
                    do {
                        input = StdIn.readLine()
                        continueGamesLoop = false

                        input.toLowerCase match {
                            case "t" => orderBy = "g.title"
                            case "d" => orderBy = "g.developer"
                            case "p" => orderBy = "g.publisher"
                            case "e" => orderBy = "g.rating"
                            case "c" => orderBy = "gpr.platform_name_fk"
                            case "r" => orderBy = "gpr.release_date"
                            case _ => {
                                println("Invalid option")
                                continueGamesLoop = true
                            }
                        }
                    } while (continueGamesLoop)

                    
                    val conn = ConnectionUtil.getConnection(db, user, pass)

                    val rs = Dao.getGames(conn, orderBy)
                    
                    println("\n-----\nGAMES\n-----")
                    println("\n[Title] - [Developer] - [Publisher] - [ESRB Rating] - [Platform] - [Release Date]\n")
                    while(rs.next()) {
                        println(s"${rs.getString("title")} - ${rs.getString("developer")} - ${rs.getString("publisher")} - " +
                                s"${rs.getString("rating")} - ${rs.getString("platform_name_fk")} - ${rs.getString("release_date")}")
                    }

                    conn.close
                }
                
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("c") => {
                    println("\nWhat would you like to sort by?")
                    println("[N] Name")
                    println("[R] Release Date")

                    var continuePlatformsLoop = false
                    var orderBy = "name"
                    do {
                        input = StdIn.readLine()
                        continuePlatformsLoop = false

                        input.toLowerCase match {
                            case "n" => orderBy = "name"
                            case "r" => orderBy = "release_date"
                            case _ => {
                                println("INVALID OPTION")
                                continuePlatformsLoop = true
                            }
                        }
                    } while (continuePlatformsLoop)

                    val conn = ConnectionUtil.getConnection(db, user, pass)
                    
                    val rs = Dao.getPlatforms(conn, orderBy)
                    println("\n---------\nPLATFORMS\n---------")
                    while(rs.next()) {
                        println(s"${rs.getString("name")} - ${rs.getString("release_date")}")
                    }

                    conn.close
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("a") => {
                    println("What would you like to add?")
                    println("[G] A game")
                    println("[C] A console/platform")
                    println("[R] An additional release for a game")

                    var continueAddLoop = false
                    do {
                        input = StdIn.readLine()
                        continueAddLoop = false

                        input.toLowerCase match {
                            case "g" => {
                                val conn = ConnectionUtil.getConnection(db, user, pass)

                                breakable {
                                    //get the title
                                    println("ENTER TITLE:")
                                    val title = StdIn.readLine()

                                    if(Dao.checkExists(conn, "games", "title", title)) {
                                            println("\nThere is already a game by that title in the database")
                                            break
                                        }
                                    
                                    //get the developer
                                    println("ENTER DEVELOPER:")
                                    val dev = StdIn.readLine()
                                    
                                    //get the publisher
                                    println("ENTER PUBLISHER:")
                                    val pub = StdIn.readLine()

                                    //get the ESRB rating
                                    println("ENTER ESRB RATING")
                                    println("[E] [E10+] [T] [M] [AO] [RP]:")
                                    var continueRatingsLoop = false
                                    var rating = "RP"
                                    do {
                                        input = StdIn.readLine()
                                        continueRatingsLoop = false

                                        input.toLowerCase match {
                                            case "e" => rating = "E"
                                            case "e10+" => rating = "E10+"
                                            case "t" => rating = "T"
                                            case "m" => rating = "M"
                                            case "ao" => rating = "AO"
                                            case "rp" => rating = "RP"
                                            case _ => {
                                                println("INVALID OPTION")
                                                continueRatingsLoop = true
                                            }
                                        }
                                    } while (continueRatingsLoop)

                                    //get the platform
                                    println("ENTER THE PLATFORM THIS TITLE RELEASED ON:")
                                    var plat = StdIn.readLine()

                                    //add the platform to the database if it doesn't exist yet
                                    Dao.addPlatformNullDate(conn, plat, java.sql.Types.DATE)

                                    //get the release date for that platform
                                    println("ENTER THE TITLE'S RELEASE DATE ON THIS PLATFORM")
                                    val release = getDateInput()

                                    //add the game to the database
                                    Dao.addGame(conn, title, dev, pub, rating)

                                    //add the release to the database
                                    Dao.addRelease(conn, title, plat, release)

                                }
                                conn.close()
                            }

                            case "c" => {
                                val conn = ConnectionUtil.getConnection(db, user, pass)
                                breakable {
                                    println("ENTER NAME:")
                                    val name = StdIn.readLine()
                                    if(Dao.checkExists(conn, "platforms", "name", name)) {
                                        println("\nThere is already a platform of that name in the database")
                                        break
                                    }

                                    println("ENTER THE PLATFORM'S RELEASE DATE")
                                    val release = getDateInput()
                                    
                                    Dao.addPlatform(conn, name, release)
                                }
                                conn.close()
                            }

                            case "r" => {
                                val conn = ConnectionUtil.getConnection(db, user, pass)

                                breakable {
                                    println("ENTER THE TITLE TO GIVE ANOTHER RELEASE")
                                    val title = StdIn.readLine()
                                    
                                    
                                    if(!Dao.checkExists(conn, "games", "title", title)) {
                                        println("\nThere is no game by that title in the database")
                                        break
                                    }

                                    println("ENTER THE OTHER PLATFORM THIS TITLE RELEASED ON")
                                    val plat = StdIn.readLine()

                                    if(!Dao.checkExists(conn, "platforms", "name", plat)) {
                                        println("\nThere is no platform by that name in the database")
                                        break
                                    }

                                    println("ENTER THE TITLE'S RELEASE DATE ON THIS PLATFORM")
                                    val release = getDateInput()

                                    Dao.addRelease(conn, title, plat, release)
                                }
                                conn.close()
                            }
                            case _ => {
                                println("INVALID OPTION")
                                continueAddLoop = true
                            }
                        }

                    } while (continueAddLoop)
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("q") => {
                    continueMenuLoop = false
                }

                case _ => {
                    println("Failed to parse command")
                }
            }
        } while (continueMenuLoop)
    }

    def printWelcome(): Unit = {
        println("\nVIDEO GAME DATABASE")
    }

    // TODO: make sure all menu options appear here
    def printMenuOptions(): Unit = {
        List(
            "\n----\nMENU\n----",
            "[G] List games",
            "[C] List consoles/platforms",
            "[A] Add to the database",
            "[Q] Quit",
            "----\n"
        ).foreach(println)
    }

    def getDateInput(): String = {
        val datePattern: Regex = "([0-9]{4})-([0-9]{2})-([0-9]{2})".r
        var release = "2000-01-01"
        var continueDateLoop = true
        do {
            var input = StdIn.readLine("YYYY-MM-DD: ")
            input match {
                case datePattern(y, m, d) => {
                    m.toInt match {
                        case 1 | 3 | 5 | 7 | 8 | 10 | 12 if (d.toInt <= 31) => {
                            release = input
                            continueDateLoop = false
                        }
                        case 2 if ((y.toInt % 4 == 0) && (d.toInt <= 29)) => {
                            release = input
                            continueDateLoop = false
                        }
                        case 2 if ((y.toInt % 4 != 0) && (d.toInt <= 28)) => {
                            release = input
                            continueDateLoop = false
                        }
                        case 4 | 6 | 9 | 11 if (d.toInt <= 30) => {
                            release = input
                            continueDateLoop = false
                        }
                        case _ => {
                            println("INVALID DATE")
                        }
                    }
                }
                case _ => {
                    println("INVALID DATE FORMAT")
                }
            }
        } while (continueDateLoop)

        release
    }
}