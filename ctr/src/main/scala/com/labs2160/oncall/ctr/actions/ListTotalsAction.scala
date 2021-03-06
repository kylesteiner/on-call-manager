package com.labs2160.oncall.ctr.actions

import java.util.Properties

import com.labs2160.oncall.ctr.resources.{DatabaseProvider, PagerDutyProvider}
import com.labs2160.slacker.api._
import com.labs2160.slacker.api.annotation.ActionDescription
import org.slf4j.LoggerFactory

@ActionDescription(
    name = "On-Call Trading Totals",
    description = "Lists the total on-call balance in hours for each team member",
    argsSpec = "",
    argsExample = ""
)
class ListTotalsAction extends Action {

    private val logger = LoggerFactory.getLogger(classOf[ListTotalsAction])
    private var database:DatabaseProvider = _
    private var api:PagerDutyProvider = _

    // For testing purposes
    def this(database: DatabaseProvider, api: PagerDutyProvider) = {
        this()
        this.database = database
        this.api = api
    }

    override def setComponents(resources: java.util.Map[String, Resource], config: Properties): Unit = {
        this.database = resources.get("OnCallDB").asInstanceOf[DatabaseProvider]
        this.api = resources.get("PagerDuty").asInstanceOf[PagerDutyProvider]
        updateUsersTable()
    }

    override def execute(ctx: SlackerContext): Boolean = {
        var response = "Total on-call balance for each team member" + '\n'
        response += listUserTotals()
        ctx.setResponseMessage(response)
        return true
    }

    def listUserTotals(): String = {
        val userTotals:List[Map[String, String]] = database.getUserTotals
        var res = ""
        for (userTotal <- userTotals) {
            val user:Map[String,String] = database.getUserFromID(userTotal("id"))
            res += user("name") + '\t' + userTotal("total") + '\n'
        }
        return res
    }

    private def updateUsersTable() = {
        val users = api.getUsers()
        for (user <- users) {
            database.updateUser(user("id"), user("name"), user("email"))
        }
    }
}