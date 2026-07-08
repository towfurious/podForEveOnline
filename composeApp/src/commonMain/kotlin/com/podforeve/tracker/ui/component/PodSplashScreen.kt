package com.podforeve.tracker.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// Warp-in splash: pod blob streaks in from top-right, then the 3 dark ports
// materialize one by one via spring-pop — like a Star Wars hyperspace exit.
// Paths auto-traced from ic_launcher_APP.png via vtracer for pixel-accurate shape.
@Composable
fun PodSplashScreen(onFinished: () -> Unit = {}) {
    val shellOffset = remember { Animatable(1f) }   // 1 = far top-right, 0 = landed
    val shellAlpha  = remember { Animatable(0f) }
    val hole1Scale  = remember { Animatable(0f) }
    val hole2Scale  = remember { Animatable(0f) }
    val hole3Scale  = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { shellOffset.animateTo(0f, tween(420, easing = EaseOutExpo)) }
        launch { shellAlpha.animateTo(1f, tween(220)) }
        delay(520L)
        launch { hole1Scale.animateTo(1f, spring(dampingRatio = 0.60f, stiffness = 480f)) }
        delay(130L)
        launch { hole2Scale.animateTo(1f, spring(dampingRatio = 0.60f, stiffness = 400f)) }
        delay(130L)
        hole3Scale.animateTo(1f, spring(dampingRatio = 0.60f, stiffness = 340f)) // await last hole
        delay(250L) // brief hold before handing off
        onFinished()
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF0C0A12))) {
        Canvas(Modifier.fillMaxSize()) {
            val scale = minOf(size.width, size.height) * 0.82f
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Warp translation: diagonal from top-right
            val warpDist = scale * 1.15f
            val tx = shellOffset.value * warpDist * COS45
            val ty = -shellOffset.value * warpDist * SIN45

            val blobCX = cx + tx
            val blobCY = cy + ty
            val alpha  = shellAlpha.value

            // Streak trail during flight
            drawWarpStreak(blobCX, blobCY, cx, cy, shellOffset.value, alpha)

            // Pod blob: fill + rim + stars + holes
            drawPodBlob(blobCX, blobCY, scale, alpha, hole1Scale.value, hole2Scale.value, hole3Scale.value)
        }
    }
}

// ── Drawing ──────────────────────────────────────────────────────────────────

private fun DrawScope.drawWarpStreak(
    fromX: Float, fromY: Float,
    toX: Float, toY: Float,
    progress: Float,
    alpha: Float,
) {
    if (progress < 0.03f) return
    val a = (progress * alpha).coerceAtMost(0.75f)
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(Color(0x00F5D060), Color(0xAAEECC60), Color(0xFFF5E070)),
            start = Offset(fromX, fromY),
            end   = Offset(toX, toY),
        ),
        start = Offset(fromX, fromY),
        end   = Offset(toX, toY),
        strokeWidth = lerp(22f, 3f, 1f - progress),
        cap = StrokeCap.Round,
        alpha = a,
    )
}

private fun DrawScope.drawPodBlob(
    cx: Float, cy: Float, sc: Float,
    alpha: Float,
    h1: Float, h2: Float, h3: Float,
) {
    // ox/oy: top-left of the normalized coordinate space (0..1 maps to 512px source)
    // blob visual centre in normalised space ≈ (0.505, 0.535)
    val ox = cx - 0.505f * sc
    val oy = cy - 0.535f * sc

    val blobPath = buildBlobPath(ox, oy, sc, sc)

    // Gradient: yellow bottom-left rim → dark purple top-right
    drawPath(
        path  = blobPath,
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFF5D060),
                0.20f to Color(0xFFDF7820),
                0.40f to Color(0xFFBF3A20),
                0.62f to Color(0xFF641C48),
                0.82f to Color(0xFF321040),
                1.00f to Color(0xFF1C0A30),
            ),
            start = Offset(ox + 0.13f * sc, oy + 0.82f * sc),
            end   = Offset(ox + 0.88f * sc, oy + 0.25f * sc),
        ),
        alpha = alpha,
    )

    clipPath(blobPath) {
        // Left-edge specular rim light
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0x55FFFBE0), Color(0x00FFFBE0)),
                start  = Offset(ox + 0.10f * sc, cy),
                end    = Offset(ox + 0.30f * sc, cy),
            ),
            size  = this.size,
            alpha = alpha,
        )

        // Star sparkles — real positions traced from SVG
        val starColor = Color(0xFFE8DCFF).copy(alpha = alpha * 0.72f)
        val stars = listOf(
            0.607f to 0.185f, 0.687f to 0.185f, 0.648f to 0.205f,
            0.490f to 0.244f, 0.627f to 0.244f, 0.666f to 0.244f,
            0.568f to 0.264f, 0.705f to 0.264f, 0.725f to 0.264f,
            0.588f to 0.283f, 0.668f to 0.283f, 0.629f to 0.303f,
            0.783f to 0.322f, 0.705f to 0.361f, 0.822f to 0.439f,
        )
        for ((nx, ny) in stars) {
            drawCircle(
                color  = starColor,
                radius = sc * 0.006f,
                center = Offset(ox + nx * sc, oy + ny * sc),
            )
        }

        // Holes — traced paths, each spring-scaled around its visual centre
        data class Hole(
            val path: Path,
            val scale: Float,
            val pivotX: Float,  // normalized pivot for scale transform
            val pivotY: Float,
        )
        val holes = listOf(
            Hole(buildHolePath1(ox, oy, sc, sc), h1, 0.500f, 0.390f),
            Hole(buildHolePath2(ox, oy, sc, sc), h2, 0.400f, 0.547f),
            Hole(buildHolePath3(ox, oy, sc, sc), h3, 0.603f, 0.653f),
        )
        val holeColor = Color(0xFF140826)
        for (hole in holes) {
            if (hole.scale <= 0f) continue
            val pvX = ox + hole.pivotX * sc
            val pvY = oy + hole.pivotY * sc
            withTransform({
                scale(hole.scale, hole.scale, Offset(pvX, pvY))
            }) {
                drawPath(hole.path, holeColor, alpha = alpha)
            }
        }
    }

    // Outer rim outline
    drawPath(
        path  = blobPath,
        color = Color(0xFFF5D060).copy(alpha = alpha * 0.18f),
        style = Stroke(width = sc * 0.004f),
    )
}

// ── Paths (traced from ic_launcher_APP.png, normalised to 0..1 / 512px) ─────

private fun buildBlobPath(ox: Float, oy: Float, w: Float, h: Float): Path = Path().apply {
    moveTo(ox + 0.481324f * w, oy + 0.257324f * h)
    cubicTo(ox+0.479277f*w,oy+0.257818f*h, ox+0.477232f*w,oy+0.258311f*h, ox+0.475125f*w,oy+0.25882f*h)
    cubicTo(ox+0.462066f*w,oy+0.262066f*h, ox+0.449873f*w,oy+0.266229f*h, ox+0.4375f*w,oy+0.271484f*h)
    cubicTo(ox+0.433557f*w,oy+0.273f*h, ox+0.429609f*w,oy+0.274506f*h, ox+0.42566f*w,oy+0.276f*h)
    cubicTo(ox+0.418328f*w,oy+0.278787f*h, ox+0.411064f*w,oy+0.281672f*h, ox+0.403861f*w,oy+0.284775f*h)
    cubicTo(ox+0.400996f*w,oy+0.286002f*h, ox+0.398131f*w,oy+0.287229f*h, ox+0.395264f*w,oy+0.288453f*h)
    cubicTo(ox+0.393844f*w,oy+0.289059f*h, ox+0.392422f*w,oy+0.289666f*h, ox+0.390959f*w,oy+0.290291f*h)
    cubicTo(ox+0.383055f*w,oy+0.293557f*h, ox+0.375998f*w,oy+0.295689f*h, ox+0.367432f*w,oy+0.296631f*h)
    cubicTo(ox+0.353863f*w,oy+0.29859f*h, ox+0.345766f*w,oy+0.307461f*h, ox+0.335938f*w,oy+0.316406f*h)
    cubicTo(ox+0.333904f*w,oy+0.318107f*h, ox+0.331863f*w,oy+0.319795f*h, ox+0.329811f*w,oy+0.321473f*h)
    cubicTo(ox+0.315535f*w,oy+0.333215f*h, ox+0.30226f*w,oy+0.345824f*h, ox+0.289184f*w,oy+0.358887f*h)
    cubicTo(ox+0.28841f*w,oy+0.35966f*h, ox+0.28841f*w,oy+0.35966f*h, ox+0.284492f*w,oy+0.363574f*h)
    cubicTo(ox+0.275217f*w,oy+0.372937f*h, ox+0.26635f*w,oy+0.382539f*h, ox+0.257812f*w,oy+0.392578f*h)
    cubicTo(ox+0.256834f*w,oy+0.393719f*h, ox+0.255854f*w,oy+0.394859f*h, ox+0.254846f*w,oy+0.396035f*h)
    cubicTo(ox+0.205902f*w,oy+0.45349f*h, ox+0.166988f*w,oy+0.520918f*h, ox+0.140889f*w,oy+0.591656f*h)
    cubicTo(ox+0.139396f*w,oy+0.595693f*h, ox+0.137877f*w,oy+0.599723f*h, ox+0.136334f*w,oy+0.60374f*h)
    cubicTo(ox+0.121648f*w,oy+0.64208f*h, ox+0.116201f*w,oy+0.674105f*h, ox+0.133369f*w,oy+0.71267f*h)
    cubicTo(ox+0.151998f*w,oy+0.749877f*h, ox+0.18776f*w,oy+0.771537f*h, ox+0.225816f*w,oy+0.785014f*h)
    cubicTo(ox+0.230965f*w,oy+0.786703f*h, ox+0.236131f*w,oy+0.788281f*h, ox+0.241332f*w,oy+0.789795f*h)
    cubicTo(ox+0.24285f*w,oy+0.790236f*h, ox+0.244367f*w,oy+0.79068f*h, ox+0.24593f*w,oy+0.791135f*h)
    cubicTo(ox+0.27951f*w,oy+0.800223f*h, ox+0.313578f*w,oy+0.802227f*h, ox+0.348158f*w,oy+0.804115f*h)
    cubicTo(ox+0.362059f*w,oy+0.804875f*h, ox+0.375953f*w,oy+0.805719f*h, ox+0.389848f*w,oy+0.806564f*h)
    cubicTo(ox+0.391227f*w,oy+0.806648f*h, ox+0.392605f*w,oy+0.806732f*h, ox+0.394027f*w,oy+0.806818f*h)
    cubicTo(ox+0.419176f*w,oy+0.80835f*h, ox+0.444273f*w,oy+0.810273f*h, ox+0.469359f*w,oy+0.812621f*h)
    cubicTo(ox+0.470176f*w,oy+0.812697f*h, ox+0.470176f*w,oy+0.812697f*h, ox+0.474305f*w,oy+0.813072f*h)
    cubicTo(ox+0.500418f*w,oy+0.815469f*h, ox+0.526734f*w,oy+0.81833f*h, ox+0.552264f*w,oy+0.824473f*h)
    cubicTo(ox+0.558562f*w,oy+0.825902f*h, ox+0.563439f*w,oy+0.826506f*h, ox+0.569832f*w,oy+0.825051f*h)
    cubicTo(ox+0.571379f*w,oy+0.824711f*h, ox+0.572926f*w,oy+0.824373f*h, ox+0.57452f*w,oy+0.824023f*h)
    cubicTo(ox+0.576072f*w,oy+0.823645f*h, ox+0.577625f*w,oy+0.823266f*h, ox+0.579223f*w,oy+0.822875f*h)
    cubicTo(ox+0.580004f*w,oy+0.822705f*h, ox+0.580004f*w,oy+0.822705f*h, ox+0.583949f*w,oy+0.821842f*h)
    cubicTo(ox+0.591475f*w,oy+0.820131f*h, ox+0.598594f*w,oy+0.8181f*h, ox+0.605758f*w,oy+0.81523f*h)
    cubicTo(ox+0.612887f*w,oy+0.812441f*h, ox+0.619395f*w,oy+0.811547f*h, ox+0.627074f*w,oy+0.811156f*h)
    cubicTo(ox+0.629313f*w,oy+0.811029f*h, ox+0.631551f*w,oy+0.8109f*h, ox+0.633857f*w,oy+0.810768f*h)
    cubicTo(ox+0.638422f*w,oy+0.810619f*h, ox+0.642996f*w,oy+0.810631f*h, ox+0.647561f*w,oy+0.810791f*h)
    cubicTo(ox+0.654861f*w,oy+0.810527f*h, ox+0.659609f*w,oy+0.808973f*h, ox+0.66626f*w,oy+0.806031f*h)
    cubicTo(ox+0.668199f*w,oy+0.805191f*h, ox+0.670137f*w,oy+0.804354f*h, ox+0.672135f*w,oy+0.80349f*h)
    cubicTo(ox+0.677734f*w,oy+0.800781f*h, ox+0.677734f*w,oy+0.800781f*h, ox+0.682441f*w,oy+0.797736f*h)
    cubicTo(ox+0.688766f*w,oy+0.794217f*h, ox+0.694709f*w,oy+0.793012f*h, ox+0.701781f*w,oy+0.791504f*h)
    cubicTo(ox+0.742107f*w,oy+0.782062f*h, ox+0.774271f*w,oy+0.759771f*h, ox+0.804688f*w,oy+0.732422f*h)
    cubicTo(ox+0.805961f*w,oy+0.731289f*h, ox+0.807236f*w,oy+0.730156f*h, ox+0.808549f*w,oy+0.728988f*h)
    cubicTo(ox+0.841297f*w,oy+0.698104f*h, ox+0.873498f*w,oy+0.651768f*h, ox+0.875656f*w,oy+0.605645f*h)
    cubicTo(ox+0.87617f*w,oy+0.576166f*h, ox+0.872045f*w,oy+0.551367f*h, ox+0.859092f*w,oy+0.524514f*h)
    cubicTo(ox+0.856492f*w,oy+0.51676f*h, ox+0.858814f*w,oy+0.511445f*h, ox+0.861328f*w,oy+0.503906f*h)
    cubicTo(ox+0.860754f*w,oy+0.501283f*h, ox+0.860102f*w,oy+0.498678f*h, ox+0.859375f*w,oy+0.496094f*h)
    cubicTo(ox+0.859375f*w,oy+0.49416f*h, ox+0.859375f*w,oy+0.492227f*h, ox+0.859375f*w,oy+0.490234f*h)
    cubicTo(ox+0.858086f*w,oy+0.490234f*h, ox+0.856797f*w,oy+0.490234f*h, ox+0.855469f*w,oy+0.490234f*h)
    cubicTo(ox+0.854824f*w,oy+0.488301f*h, ox+0.85418f*w,oy+0.486367f*h, ox+0.853516f*w,oy+0.484375f*h)
    cubicTo(ox+0.852871f*w,oy+0.48502f*h, ox+0.852227f*w,oy+0.485664f*h, ox+0.851562f*w,oy+0.486328f*h)
    cubicTo(ox+0.850273f*w,oy+0.484395f*h, ox+0.848984f*w,oy+0.482461f*h, ox+0.847656f*w,oy+0.480469f*h)
    cubicTo(ox+0.841797f*w,oy+0.476562f*h, ox+0.841797f*w,oy+0.476562f*h, ox+0.835938f*w,oy+0.474609f*h)
    cubicTo(ox+0.834648f*w,oy+0.472676f*h, ox+0.833359f*w,oy+0.470742f*h, ox+0.832031f*w,oy+0.46875f*h)
    cubicTo(ox+0.830098f*w,oy+0.468105f*h, ox+0.828164f*w,oy+0.467461f*h, ox+0.826172f*w,oy+0.466797f*h)
    cubicTo(ox+0.824883f*w,oy+0.465508f*h, ox+0.823594f*w,oy+0.464219f*h, ox+0.822266f*w,oy+0.462891f*h)
    cubicTo(ox+0.811105f*w,oy+0.453125f*h, ox+0.811105f*w,oy+0.453125f*h, ox+0.806641f*w,oy+0.453125f*h)
    cubicTo(ox+0.805795f*w,oy+0.451916f*h, ox+0.804949f*w,oy+0.450707f*h, ox+0.804078f*w,oy+0.449463f*h)
    cubicTo(ox+0.800172f*w,oy+0.444545f*h, ox+0.796906f*w,oy+0.443316f*h, ox+0.791016f*w,oy+0.441406f*h)
    cubicTo(ox+0.791016f*w,oy+0.440117f*h, ox+0.791016f*w,oy+0.438828f*h, ox+0.791016f*w,oy+0.4375f*h)
    cubicTo(ox+0.788438f*w,oy+0.4375f*h, ox+0.785859f*w,oy+0.4375f*h, ox+0.783203f*w,oy+0.4375f*h)
    cubicTo(ox+0.779945f*w,oy+0.434646f*h, ox+0.779945f*w,oy+0.434646f*h, ox+0.776977f*w,oy+0.430908f*h)
    cubicTo(ox+0.775982f*w,oy+0.429686f*h, ox+0.774988f*w,oy+0.428461f*h, ox+0.773965f*w,oy+0.427201f*h)
    cubicTo(ox+0.771484f*w,oy+0.423828f*h, ox+0.771484f*w,oy+0.423828f*h, ox+0.769531f*w,oy+0.419922f*h)
    cubicTo(ox+0.765633f*w,oy+0.418596f*h, ox+0.761727f*w,oy+0.417293f*h, ox+0.757812f*w,oy+0.416016f*h)
    cubicTo(ox+0.756523f*w,oy+0.414727f*h, ox+0.755234f*w,oy+0.413438f*h, ox+0.753906f*w,oy+0.412109f*h)
    cubicTo(ox+0.752939f*w,oy+0.411625f*h, ox+0.752939f*w,oy+0.411625f*h, ox+0.748047f*w,oy+0.40918f*h)
    cubicTo(ox+0.742541f*w,oy+0.406426f*h, ox+0.739699f*w,oy+0.403965f*h, ox+0.735719f*w,oy+0.399414f*h)
    cubicTo(ox+0.732422f*w,oy+0.396484f*h, ox+0.732422f*w,oy+0.396484f*h, ox+0.727418f*w,oy+0.394531f*h)
    cubicTo(ox+0.722656f*w,oy+0.392578f*h, ox+0.722656f*w,oy+0.392578f*h, ox+0.71875f*w,oy+0.386719f*h)
    cubicTo(ox+0.718105f*w,oy+0.387363f*h, ox+0.717461f*w,oy+0.388008f*h, ox+0.716797f*w,oy+0.388672f*h)
    cubicTo(ox+0.716152f*w,oy+0.387383f*h, ox+0.715508f*w,oy+0.386094f*h, ox+0.714844f*w,oy+0.384766f*h)
    cubicTo(ox+0.711645f*w,oy+0.382723f*h, ox+0.708379f*w,oy+0.380781f*h, ox+0.705078f*w,oy+0.378906f*h)
    cubicTo(ox+0.69901f*w,oy+0.375439f*h, ox+0.694367f*w,oy+0.372102f*h, ox+0.689453f*w,oy+0.367188f*h)
    cubicTo(ox+0.688164f*w,oy+0.367188f*h, ox+0.686875f*w,oy+0.367188f*h, ox+0.685547f*w,oy+0.367188f*h)
    cubicTo(ox+0.685104f*w,oy+0.365979f*h, ox+0.68466f*w,oy+0.36477f*h, ox+0.684203f*w,oy+0.363525f*h)
    cubicTo(ox+0.680852f*w,oy+0.358098f*h, ox+0.677791f*w,oy+0.357582f*h, ox+0.671875f*w,oy+0.355469f*h)
    cubicTo(ox+0.667916f*w,oy+0.352791f*h, ox+0.664559f*w,oy+0.350127f*h, ox+0.661254f*w,oy+0.34668f*h)
    cubicTo(ox+0.657379f*w,oy+0.342959f*h, ox+0.653049f*w,oy+0.340637f*h, ox+0.648438f*w,oy+0.337891f*h)
    cubicTo(ox+0.645168f*w,oy+0.335305f*h, ox+0.64191f*w,oy+0.332703f*h, ox+0.638672f*w,oy+0.330078f*h)
    cubicTo(ox+0.637383f*w,oy+0.329434f*h, ox+0.636094f*w,oy+0.328789f*h, ox+0.634766f*w,oy+0.328125f*h)
    cubicTo(ox+0.634121f*w,oy+0.32877f*h, ox+0.633477f*w,oy+0.329414f*h, ox+0.632812f*w,oy+0.330078f*h)
    cubicTo(ox+0.631523f*w,oy+0.328145f*h, ox+0.630234f*w,oy+0.326211f*h, ox+0.628906f*w,oy+0.324219f*h)
    cubicTo(ox+0.626973f*w,oy+0.324219f*h, ox+0.625039f*w,oy+0.324219f*h, ox+0.623047f*w,oy+0.324219f*h)
    cubicTo(ox+0.622402f*w,oy+0.322285f*h, ox+0.621758f*w,oy+0.320352f*h, ox+0.621094f*w,oy+0.318359f*h)
    cubicTo(ox+0.619805f*w,oy+0.318359f*h, ox+0.618516f*w,oy+0.318359f*h, ox+0.617188f*w,oy+0.318359f*h)
    cubicTo(ox+0.615234f*w,oy+0.315756f*h, ox+0.613281f*w,oy+0.31315f*h, ox+0.611328f*w,oy+0.310547f*h)
    cubicTo(ox+0.610039f*w,oy+0.310547f*h, ox+0.60875f*w,oy+0.310547f*h, ox+0.607422f*w,oy+0.310547f*h)
    cubicTo(ox+0.606777f*w,oy+0.309902f*h, ox+0.606777f*w,oy+0.309902f*h, ox+0.603516f*w,oy+0.306641f*h)
    cubicTo(ox+0.601297f*w,oy+0.304625f*h, ox+0.599057f*w,oy+0.302633f*h, ox+0.596801f*w,oy+0.30066f*h)
    cubicTo(ox+0.596219f*w,oy+0.300145f*h, ox+0.596219f*w,oy+0.300145f*h, ox+0.59327f*w,oy+0.297539f*h)
    cubicTo(ox+0.589844f*w,oy+0.294922f*h, ox+0.589844f*w,oy+0.294922f*h, ox+0.583984f*w,oy+0.292969f*h)
    cubicTo(ox+0.58334f*w,oy+0.29168f*h, ox+0.582695f*w,oy+0.290391f*h, ox+0.582031f*w,oy+0.289062f*h)
    cubicTo(ox+0.580098f*w,oy+0.288418f*h, ox+0.578164f*w,oy+0.287773f*h, ox+0.576172f*w,oy+0.287109f*h)
    cubicTo(ox+0.575527f*w,oy+0.286143f*h, ox+0.575527f*w,oy+0.286143f*h, ox+0.572266f*w,oy+0.28125f*h)
    cubicTo(ox+0.570332f*w,oy+0.28125f*h, ox+0.568398f*w,oy+0.28125f*h, ox+0.566406f*w,oy+0.28125f*h)
    cubicTo(ox+0.565762f*w,oy+0.279961f*h, ox+0.565117f*w,oy+0.278672f*h, ox+0.564453f*w,oy+0.277344f*h)
    cubicTo(ox+0.563244f*w,oy+0.27674f*h, ox+0.562035f*w,oy+0.276135f*h, ox+0.560791f*w,oy+0.275512f*h)
    cubicTo(ox+0.556447f*w,oy+0.273342f*h, ox+0.554188f*w,oy+0.270984f*h, ox+0.550781f*w,oy+0.267578f*h)
    cubicTo(ox+0.54917f*w,oy+0.266611f*h, ox+0.547559f*w,oy+0.265645f*h, ox+0.545898f*w,oy+0.264648f*h)
    cubicTo(ox+0.541537f*w,oy+0.262031f*h, ox+0.538547f*w,oy+0.259588f*h, ox+0.535156f*w,oy+0.255859f*h)
    cubicTo(ox+0.534512f*w,oy+0.255215f*h, ox+0.533867f*w,oy+0.25457f*h, ox+0.533203f*w,oy+0.253906f*h)
    cubicTo(ox+0.529297f*w,oy+0.253906f*h, ox+0.525391f*w,oy+0.253912f*h, ox+0.521484f*w,oy+0.253967f*h)
    cubicTo(ox+0.517309f*w,oy+0.253902f*h, ox+0.51376f*w,oy+0.253137f*h, ox+0.509766f*w,oy+0.251953f*h)
    cubicTo(ox+0.5f*w,oy+0.251953f*h, ox+0.490762f*w,oy+0.254969f*h, ox+0.481324f*w,oy+0.257324f*h)
    close()
}

private fun buildHolePath1(ox: Float, oy: Float, w: Float, h: Float): Path = Path().apply {
    moveTo(ox + 0.521484f * w, oy + 0.365234f * h)
    cubicTo(ox+0.526367f*w,oy+0.37085f*h, ox+0.526367f*w,oy+0.37085f*h, ox+0.529297f*w,oy+0.376953f*h)
    cubicTo(ox+0.528652f*w,oy+0.378887f*h, ox+0.528008f*w,oy+0.38082f*h, ox+0.527344f*w,oy+0.382812f*h)
    cubicTo(ox+0.528633f*w,oy+0.382812f*h, ox+0.529922f*w,oy+0.382812f*h, ox+0.53125f*w,oy+0.382812f*h)
    cubicTo(ox+0.532756f*w,oy+0.401918f*h, ox+0.532756f*w,oy+0.401918f*h, ox+0.527465f*w,oy+0.410277f*h)
    cubicTo(ox+0.513393f*w,oy+0.419182f*h, ox+0.498469f*w,oy+0.419041f*h, ox+0.482422f*w,oy+0.416016f*h)
    cubicTo(ox+0.475342f*w,oy+0.413453f*h, ox+0.475342f*w,oy+0.413453f*h, ox+0.470703f*w,oy+0.410156f*h)
    cubicTo(ox+0.468611f*w,oy+0.405248f*h, ox+0.468572f*w,oy+0.402922f*h, ox+0.470146f*w,oy+0.397789f*h)
    cubicTo(ox+0.477352f*w,oy+0.383172f*h, ox+0.486361f*w,oy+0.372619f*h, ox+0.501098f*w,oy+0.365234f*h)
    cubicTo(ox+0.509064f*w,oy+0.362918f*h, ox+0.513568f*w,oy+0.362695f*h, ox+0.521484f*w,oy+0.365234f*h)
    close()
}

private fun buildHolePath2(ox: Float, oy: Float, w: Float, h: Float): Path = Path().apply {
    moveTo(ox + 0.419922f * w, oy + 0.532959f * h)
    cubicTo(ox+0.423828f*w,oy+0.537109f*h, ox+0.423828f*w,oy+0.537109f*h, ox+0.425781f*w,oy+0.542969f*h)
    cubicTo(ox+0.428959f*w,oy+0.546301f*h, ox+0.432207f*w,oy+0.549566f*h, ox+0.435547f*w,oy+0.552734f*h)
    cubicTo(ox+0.434902f*w,oy+0.554023f*h, ox+0.434258f*w,oy+0.555312f*h, ox+0.433594f*w,oy+0.556641f*h)
    cubicTo(ox+0.432305f*w,oy+0.555996f*h, ox+0.431016f*w,oy+0.555352f*h, ox+0.429688f*w,oy+0.554688f*h)
    cubicTo(ox+0.435059f*w,oy+0.566162f*h, ox+0.435059f*w,oy+0.566162f*h, ox+0.439453f*w,oy+0.568359f*h)
    cubicTo(ox+0.439453f*w,oy+0.569648f*h, ox+0.439453f*w,oy+0.570937f*h, ox+0.439453f*w,oy+0.572266f*h)
    cubicTo(ox+0.41884f*w,oy+0.567418f*h, ox+0.398922f*w,oy+0.5616f*h, ox+0.378906f*w,oy+0.554688f*h)
    cubicTo(ox+0.375408f*w,oy+0.553586f*h, ox+0.371908f*w,oy+0.552486f*h, ox+0.368408f*w,oy+0.551393f*h)
    cubicTo(ox+0.361328f*w,oy+0.548828f*h, ox+0.361328f*w,oy+0.548828f*h, ox+0.359375f*w,oy+0.544922f*h)
    cubicTo(ox+0.399568f*w,oy+0.520238f*h, ox+0.399568f*w,oy+0.520238f*h, ox+0.419922f*w,oy+0.532959f*h)
    close()
}

private fun buildHolePath3(ox: Float, oy: Float, w: Float, h: Float): Path = Path().apply {
    moveTo(ox + 0.615234f * w, oy + 0.634766f * h)
    cubicTo(ox+0.621338f*w,oy+0.638305f*h, ox+0.621338f*w,oy+0.638305f*h, ox+0.625f*w,oy+0.642578f*h)
    cubicTo(ox+0.625f*w,oy+0.644512f*h, ox+0.625f*w,oy+0.646445f*h, ox+0.625f*w,oy+0.648438f*h)
    cubicTo(ox+0.626248f*w,oy+0.648961f*h, ox+0.627498f*w,oy+0.649484f*h, ox+0.628785f*w,oy+0.650023f*h)
    cubicTo(ox+0.632812f*w,oy+0.652344f*h, ox+0.632812f*w,oy+0.652344f*h, ox+0.634766f*w,oy+0.658203f*h)
    cubicTo(ox+0.633477f*w,oy+0.658203f*h, ox+0.632188f*w,oy+0.658203f*h, ox+0.630859f*w,oy+0.658203f*h)
    cubicTo(ox+0.631504f*w,oy+0.661426f*h, ox+0.632148f*w,oy+0.664648f*h, ox+0.632812f*w,oy+0.667969f*h)
    cubicTo(ox+0.634102f*w,oy+0.667969f*h, ox+0.635391f*w,oy+0.667969f*h, ox+0.636719f*w,oy+0.667969f*h)
    cubicTo(ox+0.636719f*w,oy+0.669258f*h, ox+0.636719f*w,oy+0.670547f*h, ox+0.636719f*w,oy+0.671875f*h)
    cubicTo(ox+0.627805f*w,oy+0.670947f*h, ox+0.620445f*w,oy+0.669152f*h, ox+0.612061f*w,oy+0.665795f*h)
    cubicTo(ox+0.604238f*w,oy+0.662873f*h, ox+0.596195f*w,oy+0.660654f*h, ox+0.58818f*w,oy+0.658324f*h)
    cubicTo(ox+0.581873f*w,oy+0.656197f*h, ox+0.576191f*w,oy+0.653498f*h, ox+0.570312f*w,oy+0.650391f*h)
    cubicTo(ox+0.581807f*w,oy+0.637787f*h, ox+0.598396f*w,oy+0.631488f*h, ox+0.615234f*w,oy+0.634766f*h)
    close()
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
private val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val COS45 = cos(PI.toFloat() / 4f)
private val SIN45 = sin(PI.toFloat() / 4f)
