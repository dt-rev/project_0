package com.revature.proj0.cli

import scala.util.matching.Regex
import scala.io.StdIn
import com.revature.proj0.utils.ConnectionUtil

class Cli {
    val commandArgPattern: Regex = "(\\w+)\\s*(.*)".r

    def menu(): Unit = {
        printWelcome()
        var continueMenuLoop = true
        do {
            printMenuOptions()

            var input = StdIn.readLine()
            input match {
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("test") => {
                    println("testing db connection")
                    val conn = ConnectionUtil.getConnection("jdbc:postgresql://localhost:5432/chinook",
                                                                "postgres", "postgres")
                    val stmt = conn.prepareStatement("""SELECT * FROM information_schema.tables
                                                        WHERE table_schema = 'public';""")
                    stmt.execute()

                    val rs = stmt.getResultSet()
                    while(rs.next()) {
                        println(rs.getString("table_name"))
                    }
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("exit") => {
                    continueMenuLoop = false
                }
                case _ => {
                    println("Failed to parse command")
                }
            }
        } while (continueMenuLoop)
    }

    // TODO: give the project a more accurate title
    def printWelcome(): Unit = {
        println("PROJECT 0")
    }

    // TODO: make sure all menu options appear here
    def printMenuOptions(): Unit = {
        List(
            "\n----\nMENU\n----",
            "test: test db connection",
            "exit: quit the application",
            "----\n"
        ).foreach(println)
    }
}