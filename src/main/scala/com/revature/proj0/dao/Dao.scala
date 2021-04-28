package com.revature.proj0.dao

import java.sql.Connection
import com.revature.proj0.utils.ConnectionUtil
import java.sql.ResultSet

object Dao {
    def getGames(conn: Connection, orderBy: String): ResultSet = {
        val stmt = conn.prepareStatement("SELECT g.title, g.developer, g.publisher, g.rating, gpr.platform_name_fk, gpr.release_date " +
                                                            "FROM games g LEFT JOIN games_platforms_releases gpr ON g.title = gpr.game_title_fk " +
                                                            s"ORDER BY ${orderBy};")

        stmt.execute()

        stmt.getResultSet()
    }

    def addGame(conn: Connection, title: String, developer: String, publisher: String, esrb: String): Unit = {
        val stmt = conn.prepareStatement("INSERT INTO games VALUES (?, ?, ?, ?);")
        stmt.setString(1, title)
        stmt.setString(2, developer)
        stmt.setString(3, publisher)
        stmt.setString(4, esrb)

        stmt.execute()
}

    def getPlatforms(conn: Connection, orderBy: String): ResultSet = {
        val stmt = conn.prepareStatement(s"SELECT * FROM platforms ORDER BY ${orderBy};")
        stmt.execute()

        stmt.getResultSet()
    }

    def addPlatform (conn: Connection, name: String, release: String): Unit = {
        val stmt = conn.prepareStatement("INSERT INTO platforms VALUES (?, CAST(? AS DATE)) ON CONFLICT (name) DO NOTHING;")
        stmt.setString(1, name)
        stmt.setString(2, release)

        stmt.execute()

        conn.close()
    }

    def addPlatformNullDate (conn: Connection, name: String, release: Int): Unit = {
        val stmt = conn.prepareStatement("INSERT INTO platforms VALUES (?, ?) ON CONFLICT (name) DO NOTHING;")
        stmt.setString(1, name)
        stmt.setNull(2, release)

        stmt.execute()
    }

    def addRelease (conn: Connection, gameTitle: String, platform: String, release: String): Unit = {
        val stmt = conn.prepareStatement("INSERT INTO games_platforms_releases VALUES (?, ?, CAST(? AS date));")
        stmt.setString(1, gameTitle)
        stmt.setString(2, platform)
        stmt.setString(3, release)
        
        stmt.execute()
}

    def checkExists(conn: Connection, tableName: String, columnName: String, value: String ): Boolean = {
        val stmt = conn.prepareStatement(s"SELECT EXISTS (SELECT 1 FROM ${tableName} WHERE ${columnName} = ? LIMIT 1);")
        stmt.setString(1, value)

        stmt.execute()
        val rs = stmt.getResultSet()

        rs.next()
        
        rs.getBoolean(1)
    }
}