package com.gymku.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash         : Screen("splash")
    object FirebaseSetup  : Screen("firebase_setup")
    object Login          : Screen("login")
    object Dashboard      : Screen("dashboard")
    object Scanner        : Screen("scanner")
    object MemberList     : Screen("member_list")
    object MemberDetail   : Screen("member_detail/{memberId}") {
        fun createRoute(memberId: String) = "member_detail/$memberId"
    }
    object AddMember      : Screen("add_member")
    object EditMember     : Screen("edit_member/{memberId}") {
        fun createRoute(memberId: String) = "edit_member/$memberId"
    }
    object AddVisitor     : Screen("add_visitor")
    object Report         : Screen("report")
    object AdminMenu      : Screen("admin_menu")
    object StaffSchedule  : Screen("staff_schedule")
}
