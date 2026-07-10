package com.podforeve.tracker.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// EVE Online UI icons converted from phobiacide/eve-icons SVG sprites.
// All paths are extracted from <clipPath> elements (the clip path IS the fill shape).
// Fills use Color.Black so Icon(tint=…) overrides as expected via BlendMode.SrcIn.
object EveIcons {
    val CharacterSheet: ImageVector by lazy { characterSheetIcon() }
    val Skills: ImageVector by lazy { skillsIcon() }
    val Planets: ImageVector by lazy { planetsIcon() }
    val Industry: ImageVector by lazy { industryIcon() }
    val Settings: ImageVector by lazy { settingsIcon() }
}

private val Black = SolidColor(Color.Black)
private const val VP = 128f

// Hex pilot-portrait frame — composite path from charactersheet-D clip.
private fun characterSheetIcon() = ImageVector.Builder(
    name = "EveIcons.CharacterSheet",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = VP,
    viewportHeight = VP,
).addPath(
    pathData = addPathNodes(
        "M23.5 40.221v46.765l40.499 23.383L104.5 86.986V40.221L63.999 16.838z" +
            "m12.392-1.361l1.912-1.95c6.621-6.315 15.579-10.2 25.452-10.2s18.84 3.889 25.462 10.21" +
            "c.667.633 1.31 1.29 1.928 1.971 2.006 2.222 3.735 4.679 5.149 7.318" +
            "a7.42 7.42 0 0 0-1.457-.132s-.928-.368-2.321-.368c-1.63 0-1.505 1.013-2.825 1.013" +
            "s-7.749-.798-11.093-.307c0 0-.185.214-1.659-.122-.031-.008-.058-.011-.088-.018" +
            "-1.422-.307-2.354-.094-3.737-.996-1.207-.787-3.175-1.345-4.757-1.358" +
            "-.257-.005-.514.008-.769.038-1.657.214-1.842-.828-1.72-2.088s.354-1.195.354-1.195" +
            ".562.207.729-1.062-.409-1.16-.409-1.16.218-3.644-2.76-3.644-2.762 3.644-2.762 3.644" +
            "-.576-.111-.407 1.16.727 1.062.727 1.062.231-.063.354 1.195-.06 2.302-1.718 2.088" +
            "a5.78 5.78 0 0 0-.77-.038c-1.582.013-3.55.571-4.756 1.358-1.191.776-2.599.726-3.893.893" +
            "-.239.029-.477.069-.713.121-1.474.336-1.658.122-1.658.122-3.347-.491-8.79.307-10.111.307" +
            "s-1.197-1.013-2.824-1.013c-1.394 0-2.324.368-2.324.368-.897 0-1.449.126-1.802.31" +
            " 1.436-2.718 3.205-5.247 5.266-7.527" +
            "m-9.529 24.743c-.007-5.588 1.26-11.104 3.706-16.128" +
            ".149.454 1.056 1.074 1.698 1.429.678.374.995.273 2.095.273 1.787 0 3.313-.153 4.267-.153" +
            "s4.185.788 9.682.553c1.006-.043 1.869.015 2.624.073.69.052 1.291.105 1.828.081" +
            " 2.214-.099 3.1-.491 3.1-.491.643.123 2.364 4.696 2.781 7.029s-.143 2.795-.019 4.36" +
            "-.398 2.876-.398 2.876c-.819 4.339-2.994 5.483-3.894 9.743-2.055 9.737-3.395 7.648" +
            "-4.725 12.549-1.99 7.333-2.272 8.22-2.731 9.116-.269.523-.591.799-.95.991" +
            "-11.366-6.287-19.064-18.392-19.064-32.301" +
            "m69.416-14.699c.315-.174.693-.414 1.018-.665 2.213 4.82 3.356 10.061 3.352 15.364" +
            " 0 13.073-6.805 24.549-17.059 31.102-.139-.012-.278-.036-.413-.072" +
            "-.719-.21-2.844-.404-3.302-1.301s-.812-1.756-2.277-6.146c-.83-2.493-2.391-5.58-3.155-7.987" +
            "-.418-1.316-.174-2.204-.902-5.645-.897-4.261-3.385-5.71-4.202-10.049" +
            " 0 0-.521-1.31-.398-2.876s-.437-2.026-.018-4.36 2.134-6.906 2.78-7.029" +
            "c0 0 .885.392 3.1.491.537.024 1.137-.028 1.827-.081.756-.058 1.619-.116 2.626-.073" +
            " 5.496.235 9.71-.553 10.663-.553s2.479.153 4.267.153l.916.012c.449 0 .736-.042 1.177-.285" +
            "M47.963 97.176c.042-.086.091-.166.123-.259.247-.707.215-1.044.492-1.32s.276-1.29.675-1.996" +
            " 2.118-4.053 2.825-4.943 1.228-1.934 1.749-3.838.952-2.333 1.504-2.885" +
            "c.289-.29 1.344-2.335 2.406-4.366.968-1.849 1.943-3.686 2.353-4.168" +
            ".859-1.014 1.902-2.58 2.148-3.684s.708-1.167 1.045-1.167.798.062 1.043 1.167" +
            " 1.29 2.67 2.149 3.684c.478.563 1.375 2.655 2.296 4.671.732 1.606 1.481 3.164 2.044 3.863" +
            ".49.609.54 1.167.956 3.095.857 3.976 1.848 3.106 2.999 5.09" +
            ".409.702 2.478 4.323 2.541 4.708.174 1.075.005 2.02.426 2.715" +
            "-2.049.873-4.174 1.557-6.348 2.043a36.36 36.36 0 0 1-2.371.46" +
            " 37.1 37.1 0 0 1-5.762.451c-5.277.004-10.493-1.128-15.293-3.321",
    ),
    fill = Black,
).build()

// Two stacked document pages — back (skills-C) at reduced alpha, front (skills-D) full.
private fun skillsIcon() = ImageVector.Builder(
    name = "EveIcons.Skills",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = VP,
    viewportHeight = VP,
).addPath(
    pathData = addPathNodes("M100 20.528H41.11L28 33.637v.334h9.097l8-8H94v72.397l6-6z"),
    fill = Black,
    fillAlpha = 0.65f,
).addPath(
    pathData = addPathNodes("M37.097 33.971H28v72.476h57.921V33.971z"),
    fill = Black,
).build()

// Planet body with sweeping orbit ring.
private fun planetsIcon() = ImageVector.Builder(
    name = "EveIcons.Planets",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = VP,
    viewportHeight = VP,
).addPath(
    pathData = addPathNodes(
        "M36.942 63.999l.042.987c-12.986 6.958-21.005 13.662-19.45 17.65" +
            " 2.351 6.022 25.622 1.398 51.083-8.536s44.192-22.869 41.842-28.891" +
            "c-1.52-3.895-11.502-3.029-25.252.76-5.041-5.802-12.456-9.485-20.748-9.485" +
            "-15.198 0-27.517 12.32-27.517 27.515" +
            "M87.13 48.471c8.224-1.621 13.837-1.512 14.701.705" +
            ".858 2.21-3.155 6.064-10.245 10.419a27.39 27.39 0 0 0-4.456-11.124" +
            "M25.849 78.822c-.906-2.32 3.585-6.463 11.37-11.095" +
            "a27.36 27.36 0 0 0 4.416 11.579c-4.141.868-7.676 1.324-10.354 1.324" +
            "-3.022 0-4.954-.581-5.432-1.808" +
            "m44.399-1.053c-7.913 3.088-15.556 5.737-22.692 7.912" +
            " 4.664 3.644 10.52 5.835 16.903 5.835 13.739 0 25.093-10.079 27.151-23.244" +
            "a251.21 251.21 0 0 1-21.362 9.497",
    ),
    fill = Black,
).build()

// Standard gear icon (Material Design settings path, 24×24 viewport).
private fun settingsIcon() = ImageVector.Builder(
    name = "EveIcons.Settings",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).addPath(
    pathData = addPathNodes(
        "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58" +
            "c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96" +
            "c-.5-.38-1.03-.7-1.62-.94L14.4 2.81c-.04-.24-.24-.41-.48-.41h-3.84" +
            "c-.24 0-.43.17-.47.41L9.25 5.35C8.66 5.59 8.12 5.92 7.63 6.29L5.24 5.33" +
            "c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58" +
            "C4.84 11.36 4.8 11.69 4.8 12s.02.64.07.94l-2.03 1.58" +
            "c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96" +
            "c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84" +
            "c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96" +
            "c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61L19.14 12.94z" +
            "M12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z",
    ),
    pathFillType = PathFillType.EvenOdd,
    fill = Black,
).build()

// Factory building with chimney windows cut via evenodd.
private fun industryIcon() = ImageVector.Builder(
    name = "EveIcons.Industry",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = VP,
    viewportHeight = VP,
).addPath(
    pathData = addPathNodes(
        "M91.869 89.182h-6.812v-15.57h6.812z" +
            "m-11.679 0h-6.814v-15.57h6.814z" +
            "m13.024-25.387v-7.368l-21.572 7.368v-7.368l-21.578 7.368" +
            "-3.29-34.796H37.23l-3.293 34.796-5.402 5.405v29.799h70.083V69.2z",
    ),
    pathFillType = PathFillType.EvenOdd,
    fill = Black,
).build()
