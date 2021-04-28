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
                    println("T: Title")
                    println("D: Developer")
                    println("P: Publisher")
                    println("E: ESRB Rating")
                    println("C: Console/Platform")
                    println("R: Release Date")

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

                    

                    // dao takes over here
                    //val rs = dao.getGames(orderBy)
                    val conn = ConnectionUtil.getConnection(db, user, pass)

                    val stmt = conn.prepareStatement("SELECT g.title, g.developer, g.publisher, g.rating, gpr.platform_name_fk, gpr.release_date " +
                                                            "FROM games g LEFT JOIN games_platforms_releases gpr ON g.title = gpr.game_title_fk " +
                                                            s"ORDER BY ${orderBy};")

                    stmt.execute()

                    val rs = stmt.getResultSet() // dao should return the ResultSet I think
                    
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
                    println("N: Name")
                    println("R: Release Date")

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
                    val stmt = conn.prepareStatement(s"SELECT * FROM platforms ORDER BY ${orderBy};")
                    stmt.execute()

                    val rs = stmt.getResultSet()
                    println("\n---------\nPLATFORMS\n---------")
                    while(rs.next()) {
                        println(s"${rs.getString("name")} - ${rs.getString("release_date")}")
                    }

                    conn.close
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
            "G: list of games",
            "C: list of consoles/platforms",
            "Q: quit the application",
            "----\n"
        ).foreach(println)
    }
}