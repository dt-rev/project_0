package com.revature.proj0.cli

import scala.util.matching.Regex
import scala.io.StdIn
import com.revature.proj0.utils.ConnectionUtil

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

                    

                    //dao vvv
                    //val rs = dao.getGames(orderBy)
                    val conn = ConnectionUtil.getConnection(db, user, pass)

                    val stmt = conn.prepareStatement("SELECT g.title, g.developer, g.publisher, g.rating, gpr.platform_name_fk, gpr.release_date " +
                                                            "FROM games g LEFT JOIN games_platforms_releases gpr ON g.title = gpr.game_title_fk " +
                                                            s"ORDER BY ${orderBy};")

                    stmt.execute()

                    val rs = stmt.getResultSet() // dao should return the ResultSet I think
                    //dao ^^^
                    
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
                    
                    //dao vvv
                    val conn = ConnectionUtil.getConnection(db, user, pass)
                    val stmt = conn.prepareStatement(s"SELECT * FROM platforms ORDER BY ${orderBy};")
                    stmt.execute()

                    val rs = stmt.getResultSet()
                    //dao ^^^
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
                                //get the title
                                println("ENTER TITLE:")
                                val title = StdIn.readLine()
                                
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
                                val conn = ConnectionUtil.getConnection(db, user, pass)
                                val stmt0 = conn.prepareStatement("INSERT INTO platforms VALUES (?, ?) ON CONFLICT (name) DO NOTHING;")
                                stmt0.setString(1, plat)
                                stmt0.setNull(2, java.sql.Types.DATE)

                                stmt0.execute()

                                //get the release date for that platform
                                println("ENTER THE TITLE'S RELEASE DATE ON THIS PLATFORM")
                                val datePattern: Regex = "([0-9]{4}-[0-9]{2}-[0-9]{2})".r
                                var release = "2000-01-01"
                                var continueDateLoop = true
                                do {
                                    var input = StdIn.readLine("YYYY-MM-DD: ")
                                    input match {
                                        case datePattern(d) => {
                                            release = input
                                            continueDateLoop = false
                                        }
                                        case _ => {
                                            println("INVALID DATE FORMAT")
                                        }
                                    }
                                } while (continueDateLoop)

                                //add the game to the database
                                //val conn = ConnectionUtil.getConnection(db, user, pass)
                                val stmt1 = conn.prepareStatement("INSERT INTO games VALUES (?, ?, ?, ?) ON CONFLICT (title) DO NOTHING;")
                                stmt1.setString(1, title)
                                stmt1.setString(2, dev)
                                stmt1.setString(3, pub)
                                stmt1.setString(4, rating)

                                stmt1.execute()

                                //add the release to the database
                                val stmt2 = conn.prepareStatement("INSERT INTO games_platforms_releases VALUES (?, ?, CAST(? AS date)) " +
                                                                    "ON CONFLICT ON CONSTRAINT games_platforms_releases_pkey DO NOTHING")
                                stmt2.setString(1, title)
                                stmt2.setString(2, plat)
                                stmt2.setString(3, release)
                                
                                stmt2.execute()

                                conn.close()
                            }
                            case "c" => {
                                println("ENTER NAME:")
                                val name = StdIn.readLine()

                                println("ENTER THE PLATFORM'S RELEASE DATE")
                                val datePattern: Regex = "([0-9]{4}-[0-9]{2}-[0-9]{2})".r
                                var release = "2000-01-01"
                                var continueDateLoop = true
                                do {
                                    var input = StdIn.readLine("YYYY-MM-DD: ")
                                    input match {
                                        case datePattern(d) => {
                                            release = input
                                            continueDateLoop = false
                                        }
                                        case _ => {
                                            println("INVALID DATE FORMAT")
                                        }
                                    }
                                } while (continueDateLoop)
                            }
                            case "r" => {
                                println("TODO: add an additional release for a game")
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
}