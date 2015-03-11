package com.amap.map3d.demo.basic;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.map3d.demo.R;

/**
 * AMapV2地图中介绍如何显示一个基本地图
 */
public class BasicMapActivity extends Activity {

	private MapView mapView;
	private AMap aMap;
	private static LatLng start = new LatLng(38.875967, 121.547756);
	private static LatLng end = new LatLng(38.895943, 121.562262);
	private static LatLng p1 = new LatLng(38.883717, 121.545482);
	private static LatLng p2 = new LatLng(38.884486, 121.545825);
	private static LatLng p3 = new LatLng(38.892903, 121.555696);
	private static LatLng p4 = new LatLng(38.894849, 121.558549);
	private static LatLng p5 = new LatLng(38.89545, 121.559794);
	private LatLng[] points = new LatLng[] { start, p1, p2, p3, p4, p5, end };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basicmap_activity);
		/*
		 * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
		 * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
		 * 则需要在离线地图下载和使用地图页面都进行路径设置
		 */
		// Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
		// MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写

		init();
	}

	Marker runMarker;

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		Button startBtn = (Button) findViewById(R.id.startBtn);

		startBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				runMarker.setPosition(start);
				float distance = 0;
				for (int i = 0; i < points.length; i++) {
					if (i < points.length - 1) {
						distance += AMapUtils.calculateLineDistance(points[i], points[i + 1]);
					}
				}
				Message msg = handler.obtainMessage(0);
				msg.arg2 = (int) distance;
				handler.sendMessage(msg);
				System.out.println("----->>>>distance:" + distance);

			}
		});

		if (aMap == null) {
			aMap = mapView.getMap();
			aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 15f));
			aMap.getUiSettings().setRotateGesturesEnabled(true);
		}
		adMarkers();
		MarkerOptions option = new MarkerOptions();
		option.position(start);
		option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
		runMarker = aMap.addMarker(option);

	}

	private static final int BEARING_OFFSET = 20;
	private static final int ANIMATE_SPEEED_TURN = 500;
	Handler handler = new Handler() {

		/**
		 * {@inheritDoc}
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			float disBetweenPoints = 0;
			int i = msg.what;
			int distance = msg.arg2;
			if (i < points.length - 1) {
				float bearingL = bearingBetweenLatLngs(runMarker.getPosition(), points[i + 1]);
				CameraPosition cameraPosition = new CameraPosition.Builder().target(points[i]) // changed this...
						.bearing(bearingL).tilt(90).zoom(aMap.getCameraPosition().zoom).build();

				aMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), ANIMATE_SPEEED_TURN, null);

				System.out.println("----->>>>move to marker:" + (i + 1));
				disBetweenPoints = AMapUtils.calculateLineDistance(points[i], points[i + 1]);
				long duration = (long) (disBetweenPoints / distance * 10000l);
				System.out.println("----->>>>duration:" + duration);
				animateMarker(runMarker, points[i + 1], duration);
				Message m = obtainMessage(i + 1, 0, distance);
				sendMessageDelayed(m, duration);
			}

			super.handleMessage(msg);
		}

	};

	/**
	 * Allows us to navigate to a certain point.
	 */
	public void navigateToPoint(LatLng latLng, float tilt, float bearing, float zoom, boolean animate) {
		CameraPosition position = new CameraPosition.Builder().target(latLng).zoom(zoom).bearing(bearing).tilt(tilt).build();

		changeCameraPosition(position, animate);

	}

	private void changeCameraPosition(CameraPosition cameraPosition, boolean animate) {
		CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

		if (animate) {
			aMap.animateCamera(cameraUpdate);
		} else {
			aMap.moveCamera(cameraUpdate);
		}

	}

	private Location convertLatLngToLocation(LatLng latLng) {
		Location loc = new Location("someLoc");
		loc.setLatitude(latLng.latitude);
		loc.setLongitude(latLng.longitude);
		return loc;
	}

	private float bearingBetweenLatLngs(LatLng begin, LatLng end) {
		Location beginL = convertLatLngToLocation(begin);
		Location endL = convertLatLngToLocation(end);

		return beginL.bearingTo(endL);
	}

	private void adMarkers() {
		MarkerOptions option = new MarkerOptions();
		option.position(start);
		option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
		aMap.addMarker(option);
		option = new MarkerOptions();
		option.position(end);
		option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		aMap.addMarker(option);
	}

	public void animateMarker(final Marker marker, final LatLng toPosition, final long duration) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		// Projection proj = aMap.getProjection();
		// Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = marker.getPosition();

		final Interpolator interpolator = new LinearInterpolator();

		handler.post(new Runnable() {

			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed / duration);
				double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
				double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));

				if (t < 1.0) {
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				} else {
					// if (hideMarker) {
					// marker.setVisible(true);
					// } else {
					marker.setVisible(true);
					// }
				}
			}
		});
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

}
