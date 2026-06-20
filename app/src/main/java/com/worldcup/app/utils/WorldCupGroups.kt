package com.worldcup.app.utils

// Official FIFA World Cup 2026 groups (source: Al Jazeera / FIFA official draw)
// Team names exactly as football-data.org API returns them (verified from Logcat)
object WorldCupGroups {

    data class TeamInfo(val name: String, val id: Int?)

    val groups: LinkedHashMap<String, List<TeamInfo>> = linkedMapOf(
        "Group A" to listOf(
            TeamInfo("Mexico", 769),
            TeamInfo("South Korea", 772),
            TeamInfo("South Africa", 774),
            TeamInfo("Czechia", 798)
        ),
        "Group B" to listOf(
            TeamInfo("Canada", 828),
            TeamInfo("Switzerland", 788),
            TeamInfo("Qatar", 8030),
            TeamInfo("Bosnia-Herzegovina", 1060)
        ),
        "Group C" to listOf(
            TeamInfo("Brazil", 764),
            TeamInfo("Morocco", 815),
            TeamInfo("Scotland", 8873),
            TeamInfo("Haiti", 836)
        ),
        "Group D" to listOf(
            TeamInfo("United States", 771),
            TeamInfo("Australia", 779),
            TeamInfo("Paraguay", 761),
            TeamInfo("Turkey", 803)
        ),
        "Group E" to listOf(
            TeamInfo("Germany", 759),
            TeamInfo("Ecuador", 791),
            TeamInfo("Ivory Coast", 1935),
            TeamInfo("Curaçao", 9460)
        ),
        "Group F" to listOf(
            TeamInfo("Netherlands", 8601),
            TeamInfo("Japan", 766),
            TeamInfo("Tunisia", 802),
            TeamInfo("Sweden", 792)
        ),
        "Group G" to listOf(
            TeamInfo("Belgium", 805),
            TeamInfo("Iran", 840),
            TeamInfo("Egypt", 825),
            TeamInfo("New Zealand", 783)
        ),
        "Group H" to listOf(
            TeamInfo("Spain", 760),
            TeamInfo("Uruguay", 758),
            TeamInfo("Saudi Arabia", 801),
            TeamInfo("Cape Verde Islands", 1930)
        ),
        "Group I" to listOf(
            TeamInfo("France", 773),
            TeamInfo("Senegal", 804),
            TeamInfo("Norway", 8872),
            TeamInfo("Iraq", 8062)
        ),
        "Group J" to listOf(
            TeamInfo("Argentina", 762),
            TeamInfo("Austria", 816),
            TeamInfo("Algeria", 778),
            TeamInfo("Jordan", 8049)
        ),
        "Group K" to listOf(
            TeamInfo("Portugal", 765),
            TeamInfo("Colombia", 818),
            TeamInfo("Uzbekistan", 8070),
            TeamInfo("Congo DR", 1934)
        ),
        "Group L" to listOf(
            TeamInfo("England", 770),
            TeamInfo("Croatia", 799),
            TeamInfo("Panama", 1836),
            TeamInfo("Ghana", 763)
        )
    )

    // id -> groupName for fast lookup
    val idToGroup: Map<Int, String> by lazy {
        val map = mutableMapOf<Int, String>()
        groups.forEach { (groupName, teams) ->
            teams.forEach { team ->
                team.id?.let { map[it] = groupName }
            }
        }
        map
    }
}
