package com.revature.proj0

import com.revature.proj0.cli.Cli
import com.revature.proj0.utils.ConnectionUtil
import com.revature.proj0.utils.JSONUtil
import com.revature.proj0.dao.Dao
import java.io.FileNotFoundException

object Main {
  def main(args: Array[String]):Unit = {
    val db = "jdbc:postgresql://localhost:5432/project0"
    val user = "postgres"
    val pass = "postgres"

    try {
      val data = JSONUtil.readJSON()
      val conn = ConnectionUtil.getConnection(db, user, pass)

      data.arr.foreach{ g =>
        if (!Dao.checkExists(conn, "games", "title", g("title").str)) {
          Dao.addGame(conn, g("title").str, g("developer").str, g("publisher").str, g("rating").str)
        }
      }

      conn.close()
    } catch {
      case ex: FileNotFoundException => {
        println(ex.getMessage())
        println("\nNO")
      }
    }

    val cli = new Cli()
    cli.menu()
  }

}