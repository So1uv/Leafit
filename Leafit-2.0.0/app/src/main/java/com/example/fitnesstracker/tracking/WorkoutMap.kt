package com.example.fitnesstracker.tracking

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.animation.ValueAnimator
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource

private const val EMPTY_GEOJSON = "{\"type\":\"FeatureCollection\",\"features\":[]}"

@Composable
internal fun WorkoutMap(session: LiveSession, accent: Color, recenter: Int, bottomPadding: Int,
                        readOnly: Boolean, modifier: Modifier = Modifier, onLoaded: () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val uriHandler = LocalUriHandler.current
    val view = remember {
        WorkoutMapNetwork.initialize(context)
        MapView(context, MapLibreMapOptions().textureMode(true)).apply { onCreate(Bundle()) }
    }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }
    var follow by remember { mutableStateOf(true) }
    var positioned by remember { mutableStateOf(false) }
    var fitted by remember { mutableStateOf(false) }
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    var animator by remember { mutableStateOf<ValueAnimator?>(null) }
    val onLoadedNow by rememberUpdatedState(onLoaded)
    val lastPoint by rememberUpdatedState(session.points.lastOrNull())
    val currentAccuracy by rememberUpdatedState(session.accuracy)
    val colors = MaterialTheme.colorScheme
    val canvasColor = colors.surface.toArgb()

    fun updateAccuracy() {
        val currentMap = map ?: return
        val point = lastPoint ?: return
        val metersPerPixel = currentMap.projection.getMetersPerPixelAtLatitude(point.lat).coerceAtLeast(.001)
        val radius = ((currentAccuracy ?: point.accuracy) / metersPerPixel / density.density).toFloat()
        style?.getLayerAs<CircleLayer>("leaf-accuracy")?.setProperties(circleRadius(radius.coerceIn(4f, 800f)))
    }

    DisposableEffect(owner, view) {
        var disposed = false
        var started = false
        var resumed = false
        var reportedLoaded = false
        fun sync() {
            val state = owner.lifecycle.currentState
            if (state.isAtLeast(Lifecycle.State.STARTED) && !started) { view.onStart(); started = true }
            if (state.isAtLeast(Lifecycle.State.RESUMED) && !resumed) { view.onResume(); resumed = true }
            if (!state.isAtLeast(Lifecycle.State.RESUMED) && resumed) { view.onPause(); resumed = false }
            if (!state.isAtLeast(Lifecycle.State.STARTED) && started) { view.onStop(); started = false }
        }
        val observer = LifecycleEventObserver { _, _ -> sync() }
        val memory = object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit
            override fun onLowMemory() { if (!disposed) view.onLowMemory() }
        }
        val rendered = MapView.OnDidFinishRenderingMapListener { fully ->
            if (!disposed && fully && style != null && !reportedLoaded) {
                reportedLoaded = true; onLoadedNow()
            }
        }
        val moved = MapLibreMap.OnCameraMoveStartedListener { reason ->
            if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) follow = false
        }
        val camera = MapLibreMap.OnCameraMoveListener { updateAccuracy() }
        view.addOnDidFinishRenderingMapListener(rendered)
        context.registerComponentCallbacks(memory)
        owner.lifecycle.addObserver(observer); sync()
        view.getMapAsync { currentMap ->
            if (disposed) return@getMapAsync
            map = currentMap
            currentMap.uiSettings.isCompassEnabled = false
            currentMap.uiSettings.isLogoEnabled = false
            // A visible, linked Compose attribution is placed above the HUD instead.
            currentMap.uiSettings.isAttributionEnabled = false
            currentMap.uiSettings.isTiltGesturesEnabled = false
            currentMap.setMaxZoomPreference(19.0)
            currentMap.addOnCameraMoveStartedListener(moved)
            currentMap.addOnCameraMoveListener(camera)
            currentMap.setStyle(Style.Builder().fromJson(osmStyle(canvasColor))) { loadedStyle ->
                if (!disposed) {
                    loadedStyle.getSource("osm")?.setPrefetchZoomDelta(0)
                    loadedStyle.addSource(GeoJsonSource("leaf-route", EMPTY_GEOJSON))
                    loadedStyle.addSource(GeoJsonSource("leaf-tail", EMPTY_GEOJSON))
                    loadedStyle.addSource(GeoJsonSource("leaf-position", EMPTY_GEOJSON))
                    listOf("leaf-route", "leaf-tail").forEach { id ->
                        loadedStyle.addLayer(LineLayer("$id-line", id).withProperties(
                            lineColor(accent.toArgb()), lineWidth(7f), lineCap(LINE_CAP_ROUND), lineJoin(LINE_JOIN_ROUND)))
                    }
                    loadedStyle.addLayer(CircleLayer("leaf-accuracy", "leaf-position").withProperties(
                        circleColor(accent.toArgb()), circleOpacity(.14f), circleRadius(12f), circleStrokeWidth(0f)))
                    loadedStyle.addLayer(CircleLayer("leaf-puck", "leaf-position").withProperties(
                        circleColor(accent.toArgb()), circleRadius(7f), circleStrokeWidth(2f), circleStrokeColor(android.graphics.Color.WHITE)))
                    style = loadedStyle
                }
            }
        }
        onDispose {
            disposed = true; animator?.cancel()
            owner.lifecycle.removeObserver(observer)
            context.unregisterComponentCallbacks(memory)
            view.removeOnDidFinishRenderingMapListener(rendered)
            map?.removeOnCameraMoveStartedListener(moved)
            map?.removeOnCameraMoveListener(camera)
            if (resumed) view.onPause()
            if (started) view.onStop()
            view.onDestroy()
        }
    }
    Box(modifier) {
        AndroidView(factory = { view }, modifier = Modifier.fillMaxSize().onSizeChanged { mapSize = it })
        Surface(onClick = { uriHandler.openUri("https://www.openstreetmap.org/copyright") },
            shape = CircleShape, color = colors.surfaceContainerHigh,
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = with(density) { bottomPadding.toDp() } + 6.dp)) {
            Text("© OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
        }
    }
    LaunchedEffect(map, bottomPadding, mapSize) {
        val top = minOf(with(density) { 116.dp.roundToPx() }, mapSize.height / 4)
        val bottom = minOf(bottomPadding + with(density) { 40.dp.roundToPx() }, (mapSize.height * .8f).toInt() - top).coerceAtLeast(0)
        map?.setPadding(0, top, 0, bottom)
        if (readOnly) fitted = false
    }
    LaunchedEffect(style, accent) {
        style?.getLayerAs<LineLayer>("leaf-route-line")?.setProperties(lineColor(accent.toArgb()))
        style?.getLayerAs<LineLayer>("leaf-tail-line")?.setProperties(lineColor(accent.toArgb()))
        style?.getLayerAs<CircleLayer>("leaf-accuracy")?.setProperties(circleColor(accent.toArgb()))
        style?.getLayerAs<CircleLayer>("leaf-puck")?.setProperties(circleColor(accent.toArgb()))
    }
    LaunchedEffect(style, session.accuracy) { updateAccuracy() }
    LaunchedEffect(style, session.points) {
        val loadedStyle = style ?: return@LaunchedEffect
        val route = loadedStyle.getSourceAs<GeoJsonSource>("leaf-route") ?: return@LaunchedEffect
        val tail = loadedStyle.getSourceAs<GeoJsonSource>("leaf-tail") ?: return@LaunchedEffect
        val position = loadedStyle.getSourceAs<GeoJsonSource>("leaf-position") ?: return@LaunchedEffect
        animator?.cancel()
        val last = session.points.lastOrNull()
        if (last == null) {
            route.setGeoJson(EMPTY_GEOJSON); tail.setGeoJson(EMPTY_GEOJSON); position.setGeoJson(EMPTY_GEOJSON)
            return@LaunchedEffect
        }
        val previous = session.points.getOrNull(session.points.lastIndex - 1)
        val animate = !readOnly && previous != null && previous.segment == last.segment
        val geometry = withContext(Dispatchers.Default) {
            routeJson(if (animate) session.points.dropLast(1) else session.points)
        }
        route.setGeoJson(geometry)
        tail.setGeoJson(EMPTY_GEOJSON)
        position.setGeoJson(pointJson(last.position))
        updateAccuracy()
        if (animate && previous != null) {
            val deltaLon = ((last.lon - previous.lon + 540.0) % 360.0) - 180.0
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 700
                interpolator = android.view.animation.LinearInterpolator()
                addUpdateListener { value ->
                    val f = (value.animatedValue as Float).toDouble()
                    val point = LatLng(previous.lat + (last.lat - previous.lat) * f, previous.lon + deltaLon * f)
                    tail.setGeoJson(lineJson(previous.position, point))
                    position.setGeoJson(pointJson(point))
                }
                start()
            }
        }
    }
    LaunchedEffect(map, style, session.points, follow, recenter, fitted, bottomPadding, mapSize) {
        val currentMap = map ?: return@LaunchedEffect
        if (style == null || view.width == 0 || view.height == 0) return@LaunchedEffect
        val point = session.points.lastOrNull() ?: return@LaunchedEffect
        if (readOnly && !fitted) {
            val distinct = withContext(Dispatchers.Default) { session.points.map { it.position }.distinct() }
            if (distinct.size > 1) {
                val bounds = LatLngBounds.Builder().includes(distinct).build()
                currentMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, minOf(with(density) { 32.dp.roundToPx() }, mapSize.height / 25, mapSize.width / 8).coerceAtLeast(1)))
            } else currentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point.position, 16.0))
            fitted = true
        } else if (!readOnly && follow) {
            val update = CameraUpdateFactory.newLatLngZoom(point.position, 16.0)
            if (!positioned) { currentMap.moveCamera(update); positioned = true }
            else currentMap.easeCamera(update, 700)
        }
    }
    LaunchedEffect(recenter) { follow = true; if (readOnly) fitted = false }
}

private fun osmStyle(background: Int): String = """
    {"version":8,"name":"Leafit OpenStreetMap","sources":{
      "osm":{"type":"raster","tiles":["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
        "tileSize":256,"minzoom":0,"maxzoom":19,
        "attribution":"© OpenStreetMap contributors"}},
      "layers":[{"id":"background","type":"background","paint":{"background-color":"${String.format(java.util.Locale.ROOT, "#%06X", background and 0xFFFFFF)}"}},
        {"id":"osm-map","type":"raster","source":"osm","paint":{"raster-fade-duration":180}}]}
""".trimIndent()

private fun coordinate(point: LatLng) = JSONArray().put(point.longitude).put(point.latitude)
private fun feature(geometry: JSONObject): JSONObject = JSONObject().put("type", "Feature")
    .put("properties", JSONObject()).put("geometry", geometry)
private fun pointJson(point: LatLng): String = feature(JSONObject().put("type", "Point")
    .put("coordinates", coordinate(point))).toString()
private fun lineJson(start: LatLng, end: LatLng): String = feature(JSONObject().put("type", "LineString")
    .put("coordinates", JSONArray().put(coordinate(start)).put(coordinate(end)))).toString()
private fun routeJson(points: List<TrackPoint>): String {
    val features = JSONArray()
    points.groupBy { it.segment }.values.filter { it.size > 1 }.forEach { segment ->
        val coordinates = JSONArray()
        segment.forEach { coordinates.put(coordinate(it.position)) }
        features.put(feature(JSONObject().put("type", "LineString").put("coordinates", coordinates)))
    }
    return JSONObject().put("type", "FeatureCollection").put("features", features).toString()
}
