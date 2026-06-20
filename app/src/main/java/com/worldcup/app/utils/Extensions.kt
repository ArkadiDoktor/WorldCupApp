package com.worldcup.app.utils

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun String.formatMatchDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        }
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}

fun String.formatShortDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        }
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}

fun getCountryFlag(teamName: String?): String {
    if (teamName == null) return "⚽"
    val name = teamName.lowercase()
    return when {
        name.contains("germany") -> "🇩🇪"
        name.contains("france") -> "🇫🇷"
        name.contains("brazil") -> "🇧🇷"
        name.contains("argentina") -> "🇦🇷"
        name.contains("spain") -> "🇪🇸"
        name.contains("england") -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿"
        name.contains("portugal") -> "🇵🇹"
        name.contains("netherlands") || name.contains("holland") -> "🇳🇱"
        name.contains("italy") -> "🇮🇹"
        name.contains("belgium") -> "🇧🇪"
        name.contains("croatia") -> "🇭🇷"
        name.contains("denmark") -> "🇩🇰"
        name.contains("switzerland") -> "🇨🇭"
        name.contains("austria") -> "🇦🇹"
        name.contains("poland") -> "🇵🇱"
        name.contains("czech") || name.contains("czechia") -> "🇨🇿"
        name.contains("sweden") -> "🇸🇪"
        name.contains("norway") -> "🇳🇴"
        name.contains("finland") -> "🇫🇮"
        name.contains("scotland") -> "🏴󠁧󠁢󠁳󠁣󠁴󠁿"
        name.contains("wales") -> "🏴󠁧󠁢󠁷󠁬󠁳󠁿"
        name.contains("ireland") && !name.contains("northern") -> "🇮🇪"
        name.contains("northern ireland") -> "🇬🇧"
        name.contains("turkey") || name.contains("türkiye") -> "🇹🇷"
        name.contains("ukraine") -> "🇺🇦"
        name.contains("serbia") -> "🇷🇸"
        name.contains("hungary") -> "🇭🇺"
        name.contains("slovakia") -> "🇸🇰"
        name.contains("slovenia") -> "🇸🇮"
        name.contains("albania") -> "🇦🇱"
        name.contains("romania") -> "🇷🇴"
        name.contains("bulgaria") -> "🇧🇬"
        name.contains("greece") -> "🇬🇷"
        name.contains("russia") -> "🇷🇺"
        name.contains("united states") || name.contains("usa") -> "🇺🇸"
        name.contains("mexico") -> "🇲🇽"
        name.contains("canada") -> "🇨🇦"
        name.contains("costa rica") -> "🇨🇷"
        name.contains("panama") -> "🇵🇦"
        name.contains("honduras") -> "🇭🇳"
        name.contains("el salvador") -> "🇸🇻"
        name.contains("guatemala") -> "🇬🇹"
        name.contains("jamaica") -> "🇯🇲"
        name.contains("trinidad") -> "🇹🇹"
        name.contains("cuba") -> "🇨🇺"
        name.contains("haiti") -> "🇭🇹"
        name.contains("curaçao") || name.contains("curacao") -> "🇨🇼"
        name.contains("colombia") -> "🇨🇴"
        name.contains("ecuador") -> "🇪🇨"
        name.contains("peru") -> "🇵🇪"
        name.contains("chile") -> "🇨🇱"
        name.contains("uruguay") -> "🇺🇾"
        name.contains("venezuela") -> "🇻🇪"
        name.contains("paraguay") -> "🇵🇾"
        name.contains("bolivia") -> "🇧🇴"
        name.contains("japan") -> "🇯🇵"
        name.contains("south korea") || name.contains("korea republic") -> "🇰🇷"
        name.contains("north korea") || name.contains("korea dpr") -> "🇰🇵"
        name.contains("china") -> "🇨🇳"
        name.contains("australia") -> "🇦🇺"
        name.contains("iran") -> "🇮🇷"
        name.contains("saudi arabia") -> "🇸🇦"
        name.contains("qatar") -> "🇶🇦"
        name.contains("uae") || name.contains("united arab") -> "🇦🇪"
        name.contains("iraq") -> "🇮🇶"
        name.contains("jordan") -> "🇯🇴"
        name.contains("syria") -> "🇸🇾"
        name.contains("palestine") -> "🇵🇸"
        name.contains("israel") -> "🇮🇱"
        name.contains("bahrain") -> "🇧🇭"
        name.contains("kuwait") -> "🇰🇼"
        name.contains("oman") -> "🇴🇲"
        name.contains("uzbekistan") -> "🇺🇿"
        name.contains("india") -> "🇮🇳"
        name.contains("indonesia") -> "🇮🇩"
        name.contains("thailand") -> "🇹🇭"
        name.contains("vietnam") -> "🇻🇳"
        name.contains("philippines") -> "🇵🇭"
        name.contains("malaysia") -> "🇲🇾"
        name.contains("new zealand") -> "🇳🇿"
        name.contains("morocco") -> "🇲🇦"
        name.contains("nigeria") -> "🇳🇬"
        name.contains("senegal") -> "🇸🇳"
        name.contains("ghana") -> "🇬🇭"
        name.contains("cameroon") -> "🇨🇲"
        name.contains("ivory coast") || name.contains("côte d'ivoire") -> "🇨🇮"
        name.contains("egypt") -> "🇪🇬"
        name.contains("algeria") -> "🇩🇿"
        name.contains("tunisia") -> "🇹🇳"
        name.contains("south africa") -> "🇿🇦"
        name.contains("mali") -> "🇲🇱"
        name.contains("burkina") -> "🇧🇫"
        name.contains("guinea") && !name.contains("equatorial") && !name.contains("bissau") -> "🇬🇳"
        name.contains("congo") -> "🇨🇬"
        name.contains("tanzania") -> "🇹🇿"
        name.contains("kenya") -> "🇰🇪"
        name.contains("ethiopia") -> "🇪🇹"
        name.contains("angola") -> "🇦🇴"
        name.contains("zambia") -> "🇿🇲"
        name.contains("cape verde") -> "🇨🇻"
        name.contains("bosnia") || name.contains("herzegovina") -> "🇧🇦"
        name.contains("kosovo") -> "🇽🇰"
        name.contains("iceland") -> "🇮🇸"
        name.contains("armenia") -> "🇦🇲"
        name.contains("georgia") -> "🇬🇪"
        name.contains("azerbaijan") -> "🇦🇿"
        name.contains("kazakhstan") -> "🇰🇿"
        name.contains("new caledonia") -> "🇳🇨"
        name.contains("tahiti") -> "🇵🇫"
        name.contains("fiji") -> "🇫🇯"
        name.contains("venezuela") -> "🇻🇪"
        name.contains("benin") -> "🇧🇯"
        name.contains("gabon") -> "🇬🇦"
        name.contains("mozambique") -> "🇲🇿"
        else -> "⚽"
    }
}
