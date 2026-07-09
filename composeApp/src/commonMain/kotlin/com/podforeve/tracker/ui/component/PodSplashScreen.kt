package com.podforeve.tracker.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.geometry.Size
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Warp-in splash: pod blob streaks in then 3 port holes spring-pop open.
// Blob path auto-traced from EVE capsule silhouette via Vectorizer.io (188 cubic curves).
@Composable
fun PodSplashScreen(onFinished: () -> Unit = {}) {
    val shellOffset = remember { Animatable(1f) }   // 1 = far top-right, 0 = landed
    val shellAlpha  = remember { Animatable(0f) }
    val hole1Warp  = remember { Animatable(1f) }; val hole1Alpha = remember { Animatable(0f) }
    val hole2Warp  = remember { Animatable(1f) }; val hole2Alpha = remember { Animatable(0f) }
    val hole3Warp  = remember { Animatable(1f) }; val hole3Alpha = remember { Animatable(0f) }

    // Paths cached by scale — rebuilt only when canvas size changes, not every frame.
    val cache = remember { SplashPathCache() }

    LaunchedEffect(Unit) {
        launch { shellOffset.animateTo(0f, tween(380, easing = EaseOutExpo)) }
        launch { shellAlpha.animateTo(1f, tween(200)) }
        delay(380L)
        launch { hole1Warp.animateTo(0f, tween(200, easing = EaseOutExpo)) }
        launch { hole1Alpha.animateTo(1f, tween(130)) }
        delay(110L)
        launch { hole2Warp.animateTo(0f, tween(200, easing = EaseOutExpo)) }
        launch { hole2Alpha.animateTo(1f, tween(130)) }
        delay(110L)
        launch { hole3Warp.animateTo(0f, tween(200, easing = EaseOutExpo)) }
        launch { hole3Alpha.animateTo(1f, tween(130)) }
        delay(310L)
        onFinished()
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF0C0A12))) {
        Canvas(Modifier.fillMaxSize()) {
            val sc = minOf(size.width, size.height) * 0.82f
            cache.rebuildIfNeeded(sc, size)

            val cx = size.width / 2f
            val cy = size.height / 2f
            val tx = shellOffset.value * sc * 1.15f * COS45
            val ty = -shellOffset.value * sc * 1.15f * SIN45
            val blobCX = cx + tx
            val blobCY = cy + ty
            val alpha  = shellAlpha.value

            drawWarpStreak(blobCX, blobCY, cx, cy, shellOffset.value, alpha)
            drawPodBlob(
                cx = blobCX, cy = blobCY, sc = sc, alpha = alpha,
                h1w = hole1Warp.value, h1a = hole1Alpha.value,
                h2w = hole2Warp.value, h2a = hole2Alpha.value,
                h3w = hole3Warp.value, h3a = hole3Alpha.value,
                cache = cache,
            )
        }
    }
}

// ── Mini-pod layout (edit these to reposition; both path and pivot stay in sync) ──

private const val M1_CX = 0.451f; private const val M1_CY = 0.398f; private const val M1_F = 0.068f
private const val M2_CX = 0.396f; private const val M2_CY = 0.558f; private const val M2_F = 0.140f
private const val M3_CX = 0.595f; private const val M3_CY = 0.658f; private const val M3_F = 0.118f

// ── Path cache ────────────────────────────────────────────────────────────────

private class SplashPathCache {
    var scale = 0f
    var blob  = Path()
    var hole1 = Path()
    var hole2 = Path()
    var hole3 = Path()
    var ox = 0f; var oy = 0f

    fun rebuildIfNeeded(sc: Float, canvasSize: Size) {
        if (sc == scale) return
        scale = sc
        val cx = canvasSize.width / 2f
        val cy = canvasSize.height / 2f
        // Blob centroid in 1024-normalised space = (0.5002, 0.4990)
        ox = cx - 0.5002f * sc
        oy = cy - 0.4990f * sc
        blob  = buildBlobPath(ox, oy, sc, sc)
        // Three mini-pods: same silhouette, different scale/position (depth perspective)
        hole1 = buildMiniBlob(ox, oy, sc, cx = M1_CX, cy = M1_CY, f = M1_F)
        hole2 = buildMiniBlob(ox, oy, sc, cx = M2_CX, cy = M2_CY, f = M2_F)
        hole3 = buildMiniBlob(ox, oy, sc, cx = M3_CX, cy = M3_CY, f = M3_F)
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
    drawLine(
        color = Color(0xAAF5E070),
        start = Offset(fromX, fromY),
        end   = Offset(toX, toY),
        strokeWidth = lerp(18f, 2f, 1f - progress),
        cap = StrokeCap.Round,
        alpha = (progress * alpha).coerceAtMost(0.65f),
    )
}

private fun DrawScope.drawPodBlob(
    cx: Float, cy: Float, sc: Float, alpha: Float,
    h1w: Float, h1a: Float,
    h2w: Float, h2a: Float,
    h3w: Float, h3a: Float,
    cache: SplashPathCache,
) {
    // Warp offset: shift cached (centred) geometry via transform — no new Path objects.
    val dx = cx - (cache.ox + 0.5002f * sc)
    val dy = cy - (cache.oy + 0.4990f * sc)

    withTransform({ translate(dx, dy) }) {
        drawPath(
            path  = cache.blob,
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to Color(0xFFF5D060),
                    0.20f to Color(0xFFDF7820),
                    0.40f to Color(0xFFBF3A20),
                    0.62f to Color(0xFF641C48),
                    0.82f to Color(0xFF321040),
                    1.00f to Color(0xFF1C0A30),
                ),
                start = Offset(cache.ox + 0.125f * sc, cache.oy + 0.784f * sc),
                end   = Offset(cache.ox + 0.875f * sc, cache.oy + 0.214f * sc),
            ),
            alpha = alpha,
        )

        clipPath(cache.blob) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x44FFFBE0), Color(0x00FFFBE0)),
                    start  = Offset(cache.ox + 0.095f * sc, cache.oy + 0.499f * sc),
                    end    = Offset(cache.ox + 0.275f * sc, cache.oy + 0.499f * sc),
                ),
                size  = this.size,
                alpha = alpha,
            )

            val holeColor = Color(0xFF140826)
            val warpMag   = sc * 0.40f
            val holeWarps  = floatArrayOf(h1w, h2w, h3w)
            val holeAlphas = floatArrayOf(h1a, h2a, h3a)
            val holeCXs    = floatArrayOf(M1_CX, M2_CX, M3_CX)
            val holeCYs    = floatArrayOf(M1_CY, M2_CY, M3_CY)
            val holePaths  = listOf(cache.hole1, cache.hole2, cache.hole3)
            for (i in 0..2) {
                val hAlpha = holeAlphas[i]; if (hAlpha <= 0f) continue
                val warp = holeWarps[i]
                val htx  = warp * warpMag * COS45
                val hty  = -warp * warpMag * SIN45
                val finalX = cache.ox + holeCXs[i] * sc
                val finalY = cache.oy + holeCYs[i] * sc
                if (warp > 0.02f) {
                    drawLine(
                        color       = Color(0xAAF5E070),
                        start       = Offset(finalX + htx, finalY + hty),
                        end         = Offset(finalX, finalY),
                        strokeWidth = lerp(sc * 0.014f, sc * 0.002f, 1f - warp),
                        cap         = StrokeCap.Round,
                        alpha       = (warp * hAlpha * alpha).coerceAtMost(0.6f),
                    )
                }
                withTransform({ translate(htx, hty) }) {
                    drawPath(holePaths[i], holeColor, alpha = hAlpha * alpha)
                }
            }
        }

        drawPath(
            path  = cache.blob,
            color = Color(0xFFF5D060).copy(alpha = alpha * 0.16f),
            style = Stroke(width = sc * 0.004f),
        )
    }
}

// ── Paths (traced from EVE capsule silhouette via Vectorizer.io, normalised to 0..1) ──

private fun buildBlobPath(ox: Float, oy: Float, w: Float, h: Float): Path = Path().apply {
    moveTo(ox + 0.553223f * w, oy + 0.823340f * h)
    cubicTo(ox+0.552930f*w,oy+0.822754f*h, ox+0.551172f*w,oy+0.822266f*h, ox+0.549316f*w,oy+0.822266f*h)
    cubicTo(ox+0.547461f*w,oy+0.822266f*h, ox+0.545801f*w,oy+0.821875f*h, ox+0.545508f*w,oy+0.821387f*h)
    cubicTo(ox+0.545215f*w,oy+0.820996f*h, ox+0.542676f*w,oy+0.820215f*h, ox+0.539844f*w,oy+0.819922f*h)
    cubicTo(ox+0.537012f*w,oy+0.819531f*h, ox+0.532031f*w,oy+0.818750f*h, ox+0.528809f*w,oy+0.818262f*h)
    cubicTo(ox+0.491602f*w,oy+0.812207f*h, ox+0.405859f*w,oy+0.805078f*h, ox+0.319824f*w,oy+0.800879f*h)
    cubicTo(ox+0.309082f*w,oy+0.800293f*h, ox+0.296973f*w,oy+0.799414f*h, ox+0.292969f*w,oy+0.798828f*h)
    cubicTo(ox+0.288965f*w,oy+0.798242f*h, ox+0.283496f*w,oy+0.797559f*h, ox+0.280762f*w,oy+0.797266f*h)
    cubicTo(ox+0.278125f*w,oy+0.796973f*h, ox+0.275488f*w,oy+0.796289f*h, ox+0.275000f*w,oy+0.795801f*h)
    cubicTo(ox+0.274414f*w,oy+0.795312f*h, ox+0.271973f*w,oy+0.794922f*h, ox+0.269434f*w,oy+0.794922f*h)
    cubicTo(ox+0.266895f*w,oy+0.794922f*h, ox+0.264453f*w,oy+0.794531f*h, ox+0.264160f*w,oy+0.793945f*h)
    cubicTo(ox+0.263867f*w,oy+0.793457f*h, ox+0.261816f*w,oy+0.792969f*h, ox+0.259766f*w,oy+0.792969f*h)
    cubicTo(ox+0.257715f*w,oy+0.792969f*h, ox+0.255664f*w,oy+0.792578f*h, ox+0.255371f*w,oy+0.791992f*h)
    cubicTo(ox+0.255078f*w,oy+0.791504f*h, ox+0.253320f*w,oy+0.791016f*h, ox+0.251465f*w,oy+0.791016f*h)
    cubicTo(ox+0.249609f*w,oy+0.791016f*h, ox+0.247852f*w,oy+0.790625f*h, ox+0.247559f*w,oy+0.790039f*h)
    cubicTo(ox+0.247266f*w,oy+0.789551f*h, ox+0.245703f*w,oy+0.789062f*h, ox+0.244141f*w,oy+0.789062f*h)
    cubicTo(ox+0.242578f*w,oy+0.789062f*h, ox+0.241016f*w,oy+0.788672f*h, ox+0.240723f*w,oy+0.788086f*h)
    cubicTo(ox+0.240430f*w,oy+0.787598f*h, ox+0.238867f*w,oy+0.787109f*h, ox+0.237305f*w,oy+0.787109f*h)
    cubicTo(ox+0.235742f*w,oy+0.787109f*h, ox+0.234180f*w,oy+0.786719f*h, ox+0.233887f*w,oy+0.786133f*h)
    cubicTo(ox+0.233594f*w,oy+0.785645f*h, ox+0.232227f*w,oy+0.785156f*h, ox+0.230859f*w,oy+0.785156f*h)
    cubicTo(ox+0.229590f*w,oy+0.785156f*h, ox+0.228516f*w,oy+0.784766f*h, ox+0.228516f*w,oy+0.784180f*h)
    cubicTo(ox+0.228516f*w,oy+0.783691f*h, ox+0.227246f*w,oy+0.783203f*h, ox+0.225586f*w,oy+0.783203f*h)
    cubicTo(ox+0.224023f*w,oy+0.783203f*h, ox+0.222656f*w,oy+0.782813f*h, ox+0.222656f*w,oy+0.782227f*h)
    cubicTo(ox+0.222656f*w,oy+0.781738f*h, ox+0.221582f*w,oy+0.781250f*h, ox+0.220313f*w,oy+0.781250f*h)
    cubicTo(ox+0.218945f*w,oy+0.781250f*h, ox+0.217578f*w,oy+0.780859f*h, ox+0.217285f*w,oy+0.780273f*h)
    cubicTo(ox+0.216992f*w,oy+0.779785f*h, ox+0.215820f*w,oy+0.779297f*h, ox+0.214746f*w,oy+0.779297f*h)
    cubicTo(ox+0.213770f*w,oy+0.779297f*h, ox+0.212891f*w,oy+0.778906f*h, ox+0.212891f*w,oy+0.778320f*h)
    cubicTo(ox+0.212891f*w,oy+0.777832f*h, ox+0.211816f*w,oy+0.777344f*h, ox+0.210547f*w,oy+0.777344f*h)
    cubicTo(ox+0.209180f*w,oy+0.777344f*h, ox+0.207813f*w,oy+0.776953f*h, ox+0.207520f*w,oy+0.776367f*h)
    cubicTo(ox+0.207227f*w,oy+0.775879f*h, ox+0.206055f*w,oy+0.775391f*h, ox+0.204980f*w,oy+0.775391f*h)
    cubicTo(ox+0.204004f*w,oy+0.775391f*h, ox+0.203125f*w,oy+0.775000f*h, ox+0.203125f*w,oy+0.774414f*h)
    cubicTo(ox+0.203125f*w,oy+0.773926f*h, ox+0.202246f*w,oy+0.773438f*h, ox+0.201172f*w,oy+0.773438f*h)
    cubicTo(ox+0.200098f*w,oy+0.773438f*h, ox+0.199219f*w,oy+0.773047f*h, ox+0.199219f*w,oy+0.772461f*h)
    cubicTo(ox+0.199219f*w,oy+0.771973f*h, ox+0.198340f*w,oy+0.771484f*h, ox+0.197266f*w,oy+0.771484f*h)
    cubicTo(ox+0.196191f*w,oy+0.771484f*h, ox+0.195313f*w,oy+0.771094f*h, ox+0.195313f*w,oy+0.770508f*h)
    cubicTo(ox+0.195313f*w,oy+0.770020f*h, ox+0.194434f*w,oy+0.769531f*h, ox+0.193359f*w,oy+0.769531f*h)
    cubicTo(ox+0.192285f*w,oy+0.769531f*h, ox+0.191406f*w,oy+0.769141f*h, ox+0.191406f*w,oy+0.768652f*h)
    cubicTo(ox+0.191406f*w,oy+0.768164f*h, ox+0.190625f*w,oy+0.767578f*h, ox+0.189746f*w,oy+0.767285f*h)
    cubicTo(ox+0.187500f*w,oy+0.766602f*h, ox+0.182715f*w,oy+0.763672f*h, ox+0.180273f*w,oy+0.761523f*h)
    cubicTo(ox+0.179199f*w,oy+0.760547f*h, ox+0.177734f*w,oy+0.759766f*h, ox+0.177051f*w,oy+0.759766f*h)
    cubicTo(ox+0.176367f*w,oy+0.759766f*h, ox+0.175781f*w,oy+0.759375f*h, ox+0.175781f*w,oy+0.758984f*h)
    cubicTo(ox+0.175781f*w,oy+0.758594f*h, ox+0.174609f*w,oy+0.757520f*h, ox+0.173145f*w,oy+0.756641f*h)
    cubicTo(ox+0.165820f*w,oy+0.752344f*h, ox+0.142578f*w,oy+0.729199f*h, ox+0.142578f*w,oy+0.726270f*h)
    cubicTo(ox+0.142578f*w,oy+0.725879f*h, ox+0.141699f*w,oy+0.724609f*h, ox+0.140625f*w,oy+0.723438f*h)
    cubicTo(ox+0.139551f*w,oy+0.722266f*h, ox+0.138672f*w,oy+0.720703f*h, ox+0.138672f*w,oy+0.720020f*h)
    cubicTo(ox+0.138672f*w,oy+0.719336f*h, ox+0.138281f*w,oy+0.718750f*h, ox+0.137695f*w,oy+0.718750f*h)
    cubicTo(ox+0.137207f*w,oy+0.718750f*h, ox+0.136719f*w,oy+0.717871f*h, ox+0.136719f*w,oy+0.716797f*h)
    cubicTo(ox+0.136719f*w,oy+0.715723f*h, ox+0.136328f*w,oy+0.714844f*h, ox+0.135742f*w,oy+0.714844f*h)
    cubicTo(ox+0.135254f*w,oy+0.714844f*h, ox+0.134766f*w,oy+0.713965f*h, ox+0.134766f*w,oy+0.712891f*h)
    cubicTo(ox+0.134766f*w,oy+0.711816f*h, ox+0.134375f*w,oy+0.710938f*h, ox+0.133789f*w,oy+0.710938f*h)
    cubicTo(ox+0.133301f*w,oy+0.710938f*h, ox+0.132813f*w,oy+0.710059f*h, ox+0.132813f*w,oy+0.709082f*h)
    cubicTo(ox+0.132813f*w,oy+0.708008f*h, ox+0.132520f*w,oy+0.707031f*h, ox+0.132031f*w,oy+0.706836f*h)
    cubicTo(ox+0.131152f*w,oy+0.706445f*h, ox+0.126953f*w,oy+0.694141f*h, ox+0.126953f*w,oy+0.691699f*h)
    cubicTo(ox+0.126953f*w,oy+0.690723f*h, ox+0.126563f*w,oy+0.689453f*h, ox+0.126074f*w,oy+0.688867f*h)
    cubicTo(ox+0.124609f*w,oy+0.687305f*h, ox+0.123828f*w,oy+0.678809f*h, ox+0.123828f*w,oy+0.663086f*h)
    cubicTo(ox+0.123730f*w,oy+0.647949f*h, ox+0.124414f*w,oy+0.641113f*h, ox+0.126074f*w,oy+0.640039f*h)
    cubicTo(ox+0.126563f*w,oy+0.639746f*h, ox+0.126953f*w,oy+0.638672f*h, ox+0.126953f*w,oy+0.637598f*h)
    cubicTo(ox+0.126953f*w,oy+0.636621f*h, ox+0.127344f*w,oy+0.634961f*h, ox+0.127832f*w,oy+0.633984f*h)
    cubicTo(ox+0.128320f*w,oy+0.633105f*h, ox+0.129199f*w,oy+0.630371f*h, ox+0.129883f*w,oy+0.627930f*h)
    cubicTo(ox+0.130566f*w,oy+0.625488f*h, ox+0.131445f*w,oy+0.622754f*h, ox+0.131934f*w,oy+0.621875f*h)
    cubicTo(ox+0.132422f*w,oy+0.620898f*h, ox+0.132813f*w,oy+0.619238f*h, ox+0.132813f*w,oy+0.618262f*h)
    cubicTo(ox+0.132813f*w,oy+0.617188f*h, ox+0.133203f*w,oy+0.616016f*h, ox+0.133789f*w,oy+0.615723f*h)
    cubicTo(ox+0.134277f*w,oy+0.615430f*h, ox+0.134766f*w,oy+0.613867f*h, ox+0.134766f*w,oy+0.612207f*h)
    cubicTo(ox+0.134766f*w,oy+0.610645f*h, ox+0.135254f*w,oy+0.609375f*h, ox+0.135742f*w,oy+0.609375f*h)
    cubicTo(ox+0.136328f*w,oy+0.609375f*h, ox+0.136719f*w,oy+0.608496f*h, ox+0.136719f*w,oy+0.607422f*h)
    cubicTo(ox+0.136719f*w,oy+0.606348f*h, ox+0.137109f*w,oy+0.604688f*h, ox+0.137598f*w,oy+0.603711f*h)
    cubicTo(ox+0.138086f*w,oy+0.602832f*h, ox+0.138965f*w,oy+0.600781f*h, ox+0.139648f*w,oy+0.599121f*h)
    cubicTo(ox+0.140332f*w,oy+0.597559f*h, ox+0.141211f*w,oy+0.595410f*h, ox+0.141699f*w,oy+0.594531f*h)
    cubicTo(ox+0.142188f*w,oy+0.593555f*h, ox+0.142578f*w,oy+0.591895f*h, ox+0.142578f*w,oy+0.590918f*h)
    cubicTo(ox+0.142578f*w,oy+0.589844f*h, ox+0.142969f*w,oy+0.588770f*h, ox+0.143359f*w,oy+0.588477f*h)
    cubicTo(ox+0.143750f*w,oy+0.588184f*h, ox+0.144727f*w,oy+0.585742f*h, ox+0.145508f*w,oy+0.583105f*h)
    cubicTo(ox+0.146191f*w,oy+0.580371f*h, ox+0.147168f*w,oy+0.578125f*h, ox+0.147656f*w,oy+0.578125f*h)
    cubicTo(ox+0.148047f*w,oy+0.578125f*h, ox+0.148438f*w,oy+0.577246f*h, ox+0.148438f*w,oy+0.576270f*h)
    cubicTo(ox+0.148438f*w,oy+0.575195f*h, ox+0.149316f*w,oy+0.572754f*h, ox+0.150293f*w,oy+0.570605f*h)
    cubicTo(ox+0.151367f*w,oy+0.568555f*h, ox+0.152832f*w,oy+0.565234f*h, ox+0.153711f*w,oy+0.563281f*h)
    cubicTo(ox+0.154590f*w,oy+0.561230f*h, ox+0.155859f*w,oy+0.558105f*h, ox+0.156641f*w,oy+0.556445f*h)
    cubicTo(ox+0.157422f*w,oy+0.554688f*h, ox+0.158789f*w,oy+0.551270f*h, ox+0.159863f*w,oy+0.548926f*h)
    cubicTo(ox+0.163672f*w,oy+0.539648f*h, ox+0.181641f*w,oy+0.504590f*h, ox+0.182910f*w,oy+0.504102f*h)
    cubicTo(ox+0.183301f*w,oy+0.503906f*h, ox+0.183594f*w,oy+0.502930f*h, ox+0.183594f*w,oy+0.501855f*h)
    cubicTo(ox+0.183594f*w,oy+0.500879f*h, ox+0.183887f*w,oy+0.500000f*h, ox+0.184375f*w,oy+0.500000f*h)
    cubicTo(ox+0.184766f*w,oy+0.500000f*h, ox+0.185840f*w,oy+0.498340f*h, ox+0.186719f*w,oy+0.496387f*h)
    cubicTo(ox+0.188379f*w,oy+0.492676f*h, ox+0.191504f*w,oy+0.487207f*h, ox+0.192383f*w,oy+0.486328f*h)
    cubicTo(ox+0.193359f*w,oy+0.485352f*h, ox+0.196387f*w,oy+0.479980f*h, ox+0.196973f*w,oy+0.478320f*h)
    cubicTo(ox+0.197266f*w,oy+0.477344f*h, ox+0.197852f*w,oy+0.476563f*h, ox+0.198242f*w,oy+0.476563f*h)
    cubicTo(ox+0.198633f*w,oy+0.476563f*h, ox+0.199512f*w,oy+0.475293f*h, ox+0.200195f*w,oy+0.473633f*h)
    cubicTo(ox+0.200879f*w,oy+0.472070f*h, ox+0.201758f*w,oy+0.470703f*h, ox+0.202148f*w,oy+0.470703f*h)
    cubicTo(ox+0.202539f*w,oy+0.470703f*h, ox+0.203125f*w,oy+0.469922f*h, ox+0.203418f*w,oy+0.469043f*h)
    cubicTo(ox+0.204102f*w,oy+0.466797f*h, ox+0.207031f*w,oy+0.462012f*h, ox+0.209180f*w,oy+0.459570f*h)
    cubicTo(ox+0.210156f*w,oy+0.458496f*h, ox+0.210938f*w,oy+0.457031f*h, ox+0.210938f*w,oy+0.456348f*h)
    cubicTo(ox+0.210938f*w,oy+0.455664f*h, ox+0.211328f*w,oy+0.455078f*h, ox+0.211719f*w,oy+0.455078f*h)
    cubicTo(ox+0.212207f*w,oy+0.455078f*h, ox+0.213184f*w,oy+0.453906f*h, ox+0.213965f*w,oy+0.452441f*h)
    cubicTo(ox+0.214746f*w,oy+0.450879f*h, ox+0.216113f*w,oy+0.448828f*h, ox+0.217090f*w,oy+0.447852f*h)
    cubicTo(ox+0.217969f*w,oy+0.446777f*h, ox+0.218750f*w,oy+0.445605f*h, ox+0.218750f*w,oy+0.445117f*h)
    cubicTo(ox+0.218750f*w,oy+0.444727f*h, ox+0.219336f*w,oy+0.443555f*h, ox+0.220117f*w,oy+0.442676f*h)
    cubicTo(ox+0.224121f*w,oy+0.437695f*h, ox+0.228516f*w,oy+0.431543f*h, ox+0.228516f*w,oy+0.430762f*h)
    cubicTo(ox+0.228516f*w,oy+0.430273f*h, ox+0.228906f*w,oy+0.429688f*h, ox+0.229297f*w,oy+0.429492f*h)
    cubicTo(ox+0.229688f*w,oy+0.429395f*h, ox+0.231543f*w,oy+0.427246f*h, ox+0.233398f*w,oy+0.424805f*h)
    cubicTo(ox+0.235254f*w,oy+0.422363f*h, ox+0.237012f*w,oy+0.420215f*h, ox+0.237305f*w,oy+0.419922f*h)
    cubicTo(ox+0.237598f*w,oy+0.419629f*h, ox+0.238965f*w,oy+0.417871f*h, ox+0.240234f*w,oy+0.416016f*h)
    cubicTo(ox+0.241504f*w,oy+0.414160f*h, ox+0.243164f*w,oy+0.412012f*h, ox+0.243848f*w,oy+0.411328f*h)
    cubicTo(ox+0.244531f*w,oy+0.410547f*h, ox+0.246387f*w,oy+0.408398f*h, ox+0.247852f*w,oy+0.406445f*h)
    cubicTo(ox+0.249316f*w,oy+0.404395f*h, ox+0.251172f*w,oy+0.402148f*h, ox+0.251953f*w,oy+0.401367f*h)
    cubicTo(ox+0.252832f*w,oy+0.400586f*h, ox+0.254980f*w,oy+0.397949f*h, ox+0.256836f*w,oy+0.395508f*h)
    cubicTo(ox+0.258691f*w,oy+0.393066f*h, ox+0.262402f*w,oy+0.388672f*h, ox+0.265234f*w,oy+0.385742f*h)
    cubicTo(ox+0.267969f*w,oy+0.382813f*h, ox+0.274219f*w,oy+0.375977f*h, ox+0.279102f*w,oy+0.370508f*h)
    cubicTo(ox+0.288281f*w,oy+0.360352f*h, ox+0.314941f*w,oy+0.334473f*h, ox+0.328613f*w,oy+0.322559f*h)
    cubicTo(ox+0.370898f*w,oy+0.285645f*h, ox+0.413574f*w,oy+0.255078f*h, ox+0.447266f*w,oy+0.237598f*h)
    cubicTo(ox+0.453418f*w,oy+0.234375f*h, ox+0.465723f*w,oy+0.227930f*h, ox+0.474609f*w,oy+0.223340f*h)
    cubicTo(ox+0.510742f*w,oy+0.204492f*h, ox+0.558301f*w,oy+0.185352f*h, ox+0.587305f*w,oy+0.177832f*h)
    cubicTo(ox+0.598828f*w,oy+0.174805f*h, ox+0.618555f*w,oy+0.173535f*h, ox+0.643555f*w,oy+0.174121f*h)
    cubicTo(ox+0.667285f*w,oy+0.174707f*h, ox+0.676953f*w,oy+0.175879f*h, ox+0.694922f*w,oy+0.180566f*h)
    cubicTo(ox+0.729004f*w,oy+0.189355f*h, ox+0.758398f*w,oy+0.208008f*h, ox+0.772852f*w,oy+0.229883f*h)
    cubicTo(ox+0.779980f*w,oy+0.240723f*h, ox+0.782129f*w,oy+0.245215f*h, ox+0.783301f*w,oy+0.251758f*h)
    cubicTo(ox+0.784766f*w,oy+0.260059f*h, ox+0.785059f*w,oy+0.260645f*h, ox+0.789355f*w,oy+0.265332f*h)
    cubicTo(ox+0.791895f*w,oy+0.268066f*h, ox+0.796387f*w,oy+0.271191f*h, ox+0.801270f*w,oy+0.273633f*h)
    cubicTo(ox+0.821387f*w,oy+0.283789f*h, ox+0.830859f*w,oy+0.293652f*h, ox+0.842188f*w,oy+0.316211f*h)
    cubicTo(ox+0.846777f*w,oy+0.325293f*h, ox+0.850586f*w,oy+0.337695f*h, ox+0.850488f*w,oy+0.343262f*h)
    cubicTo(ox+0.850488f*w,oy+0.348828f*h, ox+0.847852f*w,oy+0.353906f*h, ox+0.842773f*w,oy+0.358301f*h)
    cubicTo(ox+0.836621f*w,oy+0.363574f*h, ox+0.834766f*w,oy+0.367090f*h, ox+0.834766f*w,oy+0.373242f*h)
    cubicTo(ox+0.834766f*w,oy+0.379980f*h, ox+0.837793f*w,oy+0.383594f*h, ox+0.847852f*w,oy+0.388477f*h)
    cubicTo(ox+0.852344f*w,oy+0.390625f*h, ox+0.857227f*w,oy+0.392871f*h, ox+0.858691f*w,oy+0.393457f*h)
    cubicTo(ox+0.862891f*w,oy+0.395117f*h, ox+0.867773f*w,oy+0.406250f*h, ox+0.869531f*w,oy+0.418457f*h)
    cubicTo(ox+0.870410f*w,oy+0.423828f*h, ox+0.871484f*w,oy+0.430566f*h, ox+0.872070f*w,oy+0.433496f*h)
    cubicTo(ox+0.872559f*w,oy+0.436328f*h, ox+0.873047f*w,oy+0.445801f*h, ox+0.873047f*w,oy+0.454492f*h)
    cubicTo(ox+0.873047f*w,oy+0.473242f*h, ox+0.871289f*w,oy+0.482813f*h, ox+0.865332f*w,oy+0.498145f*h)
    cubicTo(ox+0.861719f*w,oy+0.507520f*h, ox+0.861328f*w,oy+0.509180f*h, ox+0.861328f*w,oy+0.517383f*h)
    cubicTo(ox+0.861328f*w,oy+0.525488f*h, ox+0.861719f*w,oy+0.527344f*h, ox+0.865137f*w,oy+0.536426f*h)
    cubicTo(ox+0.873242f*w,oy+0.557617f*h, ox+0.876563f*w,oy+0.576855f*h, ox+0.875684f*w,oy+0.597363f*h)
    cubicTo(ox+0.875293f*w,oy+0.604980f*h, ox+0.874609f*w,oy+0.613281f*h, ox+0.874023f*w,oy+0.615918f*h)
    cubicTo(ox+0.873438f*w,oy+0.618457f*h, ox+0.872559f*w,oy+0.622852f*h, ox+0.871973f*w,oy+0.625488f*h)
    cubicTo(ox+0.869922f*w,oy+0.635156f*h, ox+0.867090f*w,oy+0.643848f*h, ox+0.863379f*w,oy+0.651172f*h)
    cubicTo(ox+0.862305f*w,oy+0.653516f*h, ox+0.861328f*w,oy+0.656055f*h, ox+0.861328f*w,oy+0.656836f*h)
    cubicTo(ox+0.861328f*w,oy+0.657617f*h, ox+0.861035f*w,oy+0.658203f*h, ox+0.860547f*w,oy+0.658203f*h)
    cubicTo(ox+0.860156f*w,oy+0.658203f*h, ox+0.859082f*w,oy+0.659863f*h, ox+0.858203f*w,oy+0.661914f*h)
    cubicTo(ox+0.856055f*w,oy+0.666504f*h, ox+0.853516f*w,oy+0.670801f*h, ox+0.851367f*w,oy+0.673242f*h)
    cubicTo(ox+0.850391f*w,oy+0.674316f*h, ox+0.849609f*w,oy+0.675684f*h, ox+0.849609f*w,oy+0.676270f*h)
    cubicTo(ox+0.849609f*w,oy+0.676855f*h, ox+0.848730f*w,oy+0.677930f*h, ox+0.847656f*w,oy+0.678711f*h)
    cubicTo(ox+0.846582f*w,oy+0.679492f*h, ox+0.845703f*w,oy+0.680566f*h, ox+0.845703f*w,oy+0.681152f*h)
    cubicTo(ox+0.845703f*w,oy+0.681641f*h, ox+0.844629f*w,oy+0.683398f*h, ox+0.843262f*w,oy+0.684961f*h)
    cubicTo(ox+0.841895f*w,oy+0.686523f*h, ox+0.840820f*w,oy+0.688184f*h, ox+0.840820f*w,oy+0.688574f*h)
    cubicTo(ox+0.840820f*w,oy+0.688965f*h, ox+0.840234f*w,oy+0.689941f*h, ox+0.839551f*w,oy+0.690625f*h)
    cubicTo(ox+0.838867f*w,oy+0.691309f*h, ox+0.837207f*w,oy+0.693457f*h, ox+0.835938f*w,oy+0.695313f*h)
    cubicTo(ox+0.834668f*w,oy+0.697168f*h, ox+0.832910f*w,oy+0.699414f*h, ox+0.832031f*w,oy+0.700195f*h)
    cubicTo(ox+0.831152f*w,oy+0.700977f*h, ox+0.829395f*w,oy+0.703223f*h, ox+0.828125f*w,oy+0.705078f*h)
    cubicTo(ox+0.826758f*w,oy+0.706934f*h, ox+0.824219f*w,oy+0.710059f*h, ox+0.822461f*w,oy+0.711914f*h)
    cubicTo(ox+0.820605f*w,oy+0.713770f*h, ox+0.817578f*w,oy+0.717285f*h, ox+0.815625f*w,oy+0.719727f*h)
    cubicTo(ox+0.810938f*w,oy+0.725293f*h, ox+0.803125f*w,oy+0.733203f*h, ox+0.798828f*w,oy+0.736523f*h)
    cubicTo(ox+0.796973f*w,oy+0.737891f*h, ox+0.793848f*w,oy+0.740527f*h, ox+0.791992f*w,oy+0.742383f*h)
    cubicTo(ox+0.790137f*w,oy+0.744141f*h, ox+0.787012f*w,oy+0.746680f*h, ox+0.785156f*w,oy+0.748047f*h)
    cubicTo(ox+0.783301f*w,oy+0.749316f*h, ox+0.781055f*w,oy+0.751172f*h, ox+0.780176f*w,oy+0.752148f*h)
    cubicTo(ox+0.779297f*w,oy+0.753125f*h, ox+0.778223f*w,oy+0.753906f*h, ox+0.777734f*w,oy+0.753906f*h)
    cubicTo(ox+0.777246f*w,oy+0.753906f*h, ox+0.775293f*w,oy+0.755273f*h, ox+0.773438f*w,oy+0.756836f*h)
    cubicTo(ox+0.771582f*w,oy+0.758398f*h, ox+0.769629f*w,oy+0.759766f*h, ox+0.769238f*w,oy+0.759766f*h)
    cubicTo(ox+0.768848f*w,oy+0.759766f*h, ox+0.767578f*w,oy+0.760645f*h, ox+0.766406f*w,oy+0.761719f*h)
    cubicTo(ox+0.765234f*w,oy+0.762793f*h, ox+0.763672f*w,oy+0.763672f*h, ox+0.762988f*w,oy+0.763672f*h)
    cubicTo(ox+0.762305f*w,oy+0.763672f*h, ox+0.761719f*w,oy+0.764160f*h, ox+0.761719f*w,oy+0.764648f*h)
    cubicTo(ox+0.761719f*w,oy+0.765234f*h, ox+0.761133f*w,oy+0.765625f*h, ox+0.760449f*w,oy+0.765625f*h)
    cubicTo(ox+0.759766f*w,oy+0.765625f*h, ox+0.758203f*w,oy+0.766504f*h, ox+0.757031f*w,oy+0.767578f*h)
    cubicTo(ox+0.755859f*w,oy+0.768652f*h, ox+0.754297f*w,oy+0.769531f*h, ox+0.753516f*w,oy+0.769531f*h)
    cubicTo(ox+0.752734f*w,oy+0.769531f*h, ox+0.751953f*w,oy+0.769824f*h, ox+0.751758f*w,oy+0.770313f*h)
    cubicTo(ox+0.751367f*w,oy+0.771387f*h, ox+0.735449f*w,oy+0.779297f*h, ox+0.733789f*w,oy+0.779297f*h)
    cubicTo(ox+0.733008f*w,oy+0.779297f*h, ox+0.732422f*w,oy+0.779785f*h, ox+0.732422f*w,oy+0.780273f*h)
    cubicTo(ox+0.732422f*w,oy+0.780859f*h, ox+0.731738f*w,oy+0.781250f*h, ox+0.730957f*w,oy+0.781250f*h)
    cubicTo(ox+0.730176f*w,oy+0.781250f*h, ox+0.728711f*w,oy+0.781641f*h, ox+0.727734f*w,oy+0.782129f*h)
    cubicTo(ox+0.726855f*w,oy+0.782617f*h, ox+0.725000f*w,oy+0.783398f*h, ox+0.723633f*w,oy+0.783887f*h)
    cubicTo(ox+0.722266f*w,oy+0.784473f*h, ox+0.720117f*w,oy+0.785352f*h, ox+0.718750f*w,oy+0.785938f*h)
    cubicTo(ox+0.717383f*w,oy+0.786426f*h, ox+0.714355f*w,oy+0.787402f*h, ox+0.711914f*w,oy+0.788086f*h)
    cubicTo(ox+0.709473f*w,oy+0.788770f*h, ox+0.706641f*w,oy+0.789648f*h, ox+0.705566f*w,oy+0.790039f*h)
    cubicTo(ox+0.703516f*w,oy+0.790918f*h, ox+0.700195f*w,oy+0.791602f*h, ox+0.691895f*w,oy+0.792773f*h)
    cubicTo(ox+0.687793f*w,oy+0.793359f*h, ox+0.681543f*w,oy+0.795996f*h, ox+0.679688f*w,oy+0.797852f*h)
    cubicTo(ox+0.678516f*w,oy+0.799023f*h, ox+0.670605f*w,oy+0.802734f*h, ox+0.669336f*w,oy+0.802734f*h)
    cubicTo(ox+0.668555f*w,oy+0.802734f*h, ox+0.667969f*w,oy+0.803223f*h, ox+0.667969f*w,oy+0.803711f*h)
    cubicTo(ox+0.667969f*w,oy+0.804297f*h, ox+0.667285f*w,oy+0.804688f*h, ox+0.666504f*w,oy+0.804688f*h)
    cubicTo(ox+0.665723f*w,oy+0.804688f*h, ox+0.664258f*w,oy+0.805078f*h, ox+0.663281f*w,oy+0.805566f*h)
    cubicTo(ox+0.662402f*w,oy+0.806055f*h, ox+0.660449f*w,oy+0.806934f*h, ox+0.658984f*w,oy+0.807520f*h)
    cubicTo(ox+0.657422f*w,oy+0.808105f*h, ox+0.655176f*w,oy+0.809082f*h, ox+0.653906f*w,oy+0.809668f*h)
    cubicTo(ox+0.652441f*w,oy+0.810352f*h, ox+0.649902f*w,oy+0.810449f*h, ox+0.647363f*w,oy+0.810059f*h)
    cubicTo(ox+0.641992f*w,oy+0.809180f*h, ox+0.628418f*w,oy+0.809180f*h, ox+0.620117f*w,oy+0.809961f*h)
    cubicTo(ox+0.614258f*w,oy+0.810645f*h, ox+0.611621f*w,oy+0.811328f*h, ox+0.607227f*w,oy+0.813574f*h)
    cubicTo(ox+0.606250f*w,oy+0.814063f*h, ox+0.604590f*w,oy+0.814453f*h, ox+0.603613f*w,oy+0.814453f*h)
    cubicTo(ox+0.602539f*w,oy+0.814453f*h, ox+0.601367f*w,oy+0.814941f*h, ox+0.601074f*w,oy+0.815430f*h)
    cubicTo(ox+0.600781f*w,oy+0.816016f*h, ox+0.599414f*w,oy+0.816406f*h, ox+0.598145f*w,oy+0.816406f*h)
    cubicTo(ox+0.596875f*w,oy+0.816406f*h, ox+0.595508f*w,oy+0.816895f*h, ox+0.595215f*w,oy+0.817383f*h)
    cubicTo(ox+0.594922f*w,oy+0.817969f*h, ox+0.593359f*w,oy+0.818359f*h, ox+0.591895f*w,oy+0.818359f*h)
    cubicTo(ox+0.590430f*w,oy+0.818359f*h, ox+0.588867f*w,oy+0.818750f*h, ox+0.588379f*w,oy+0.819238f*h)
    cubicTo(ox+0.587793f*w,oy+0.819727f*h, ox+0.585645f*w,oy+0.820313f*h, ox+0.583496f*w,oy+0.820605f*h)
    cubicTo(ox+0.581348f*w,oy+0.820996f*h, ox+0.577148f*w,oy+0.821680f*h, ox+0.574219f*w,oy+0.822266f*h)
    cubicTo(ox+0.566211f*w,oy+0.823926f*h, ox+0.554004f*w,oy+0.824512f*h, ox+0.553223f*w,oy+0.823340f*h)
    close()
}

// Mini-pod: same silhouette as main blob, centred at (cx, cy) in normalised space, scaled by f.
private fun buildMiniBlob(ox: Float, oy: Float, sc: Float, cx: Float, cy: Float, f: Float): Path {
    val s = sc * f
    return buildBlobPath(ox + cx * sc - 0.5002f * s, oy + cy * sc - 0.4990f * s, s, s)
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
private val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val COS45 = cos(PI.toFloat() / 4f)
private val SIN45 = sin(PI.toFloat() / 4f)
