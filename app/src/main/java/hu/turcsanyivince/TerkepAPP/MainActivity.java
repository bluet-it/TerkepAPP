package hu.turcsanyivince.TerkepAPP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.commons.geojson.Polygon;
import com.mapbox.services.commons.geojson.custom.PositionDeserializer;
import com.mapbox.services.commons.models.Position;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class MainActivity extends AppCompatActivity implements
		OnMapReadyCallback, PermissionsListener,
		Items.ItemClickListener, View.OnClickListener, MapboxMap.OnMapClickListener, AdapterView.OnItemSelectedListener {
	private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
	private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
	private static final String PROPERTY_NAME = "name";
	private static MapView mapView;
	public ArrayList<Double> latitudes = new ArrayList<>();
	public ArrayList<Double> longitudes = new ArrayList<>();
	public ArrayList<Double> latitudes_search = new ArrayList<>();
	public ArrayList<Double> longitudes_search = new ArrayList<>();
	public ArrayList<String> json = new ArrayList<>();
	public MapboxMap mapboxMap;
	// Variables needed to initialize a map
	boolean dark = false;
	Point location = null;
	double lastLat = 0;
	double lastLon = 0;
	FeatureCollection polygon;
	String selected_json = "";
	String geoJson = "";
	Items adapter;
	Items adapter_search;
	int TOAST_DURATION = 5000;
	Toast onBackPressedToast;
	// Create the Handler object (on the main thread by default)
	Handler handler = new Handler();
	private GeoJsonSource source;
	private FeatureCollection featureCollection;
	//private MapView mapView;
	// Variables needed to handle location permissions
	private PermissionsManager permissionsManager;
	// Variables needed to add the location engine
	private LocationEngine locationEngine;
	private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
	private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
	// Variables needed to listen to location updates
	private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);
	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= item -> {
		switch (item.getItemId()) {
			case R.id.navigation_map:
				findViewById(R.id.linearLayout).setVisibility(View.GONE);
				findViewById(R.id.search_results).setVisibility(View.GONE);
				findViewById(R.id.center_map).setVisibility(View.VISIBLE);
				findViewById(R.id.compass_map).setVisibility(View.VISIBLE);
				return true;
			case R.id.navigation_list:
				findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
				findViewById(R.id.SearchLayout).setVisibility(View.GONE);
				findViewById(R.id.FilterLayout).setVisibility(View.GONE);
				findViewById(R.id.search_results).setVisibility(View.GONE);
				findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
				findViewById(R.id.center_map).setVisibility(View.GONE);
				findViewById(R.id.compass_map).setVisibility(View.GONE);
				return true;
			case R.id.navigation_search:
				findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
				findViewById(R.id.SearchLayout).setVisibility(View.VISIBLE);
				findViewById(R.id.recyclerView).setVisibility(View.GONE);
				findViewById(R.id.FilterLayout).setVisibility(View.GONE);
				findViewById(R.id.search_results).setVisibility(View.VISIBLE);
				findViewById(R.id.center_map).setVisibility(View.GONE);
				findViewById(R.id.compass_map).setVisibility(View.GONE);
				return true;
			case R.id.navigation_filter:
				findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
				findViewById(R.id.SearchLayout).setVisibility(View.GONE);
				findViewById(R.id.recyclerView).setVisibility(View.GONE);
				findViewById(R.id.FilterLayout).setVisibility(View.VISIBLE);
				findViewById(R.id.search_results).setVisibility(View.VISIBLE);
				findViewById(R.id.center_map).setVisibility(View.GONE);
				findViewById(R.id.compass_map).setVisibility(View.GONE);
				return true;
		}
		return false;
	};
	private long mLastPress = 0;
	// Define the code block to be executed
	private Runnable update = new Runnable() {
		@Override
		public void run () {
			updateMap();
			handler.postDelayed(this, 10000);
		}
	};

	void updateMap () {
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run () {
				{
					Switch update = findViewById(R.id.auto);
					if (update.isChecked()) {
						try {
							Switch order = findViewById(R.id.order);
							Button button = findViewById(R.id.search_distance);
							if (mapboxMap.getLocationComponent().getLastKnownLocation() != null) {
								location = Point.fromLngLat(
										mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude(),
										mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude());
								order.setVisibility(View.VISIBLE);
								button.setVisibility(View.VISIBLE);
							} else {
								order.setVisibility(View.GONE);
								button.setVisibility(View.GONE);
							}

							FeatureCollection featureCollection = FeatureCollection.fromJson(geoJson);

							ArrayList<String> Places = new ArrayList<>();

							ArrayList<Double> lats = new ArrayList<>();
							ArrayList<Double> lons = new ArrayList<>();
							ArrayList<String> data_json = new ArrayList<>();

							int i = 0;
							for (Feature singleFeature : featureCollection.features()) {
								if (singleFeature.getStringProperty(PROPERTY_NAME) != null) {
									String place = "";
									if (location != null && order.isChecked()) {
										double distance = new LatLng(location.latitude(), location.longitude()).distanceTo(
												new LatLng(Double.parseDouble(singleFeature.toJson()
														.split("\"coordinates\":\\[")[1]
														.split(",")[1]
														.split("]")[0]),
														Double.parseDouble(singleFeature.toJson()
																.split("\"coordinates\":\\[")[1]
																.split(",")[0])));
										if (distance > Integer.MAX_VALUE) {
											distance = Integer.MAX_VALUE;
										}
										place += distance;
										place += "\r";
									}
									place += singleFeature.getStringProperty(PROPERTY_NAME);
									if (!Objects.equals(singleFeature.getStringProperty("addr:postcode"), null)) {
										place += " (" + singleFeature.getStringProperty("addr:postcode") + " ";
									}
									if (!Objects.equals(singleFeature.getStringProperty("addr:city"), null)) {
										place += singleFeature.getStringProperty("addr:city");
									}
									if (!Objects.equals(singleFeature.getStringProperty("addr:street"), null)) {
										place += ", " + singleFeature.getStringProperty("addr:street");
										if (!Objects.equals(singleFeature.getStringProperty("addr:housenumber"), null)) {
											place += " " + singleFeature.getStringProperty("addr:housenumber") + ".";
										}
									}
									if (!Objects.equals(singleFeature.getStringProperty("addr:city"), null)) {
										place += ")";
									}
									if (location != null) {
										double distance = Math.round(new LatLng(location.latitude(), location.longitude()).distanceTo(
												new LatLng(Double.parseDouble(singleFeature.toJson()
														.split("\"coordinates\":\\[")[1]
														.split(",")[1]
														.split("]")[0]),
														Double.parseDouble(((singleFeature.toJson()
																.split("\"coordinates\":\\[")[1]
																.split(",")[0]))))));
										if (!(distance > Integer.MAX_VALUE)) {
											place += " (";
											place += Math.round(distance);
											place += " m)";
										}
									}
									Places.add(place + "\n" + i);


									lats.add(Double.parseDouble(singleFeature.toJson()
											.split("\"coordinates\":\\[")[1]
											.split(",")[1]
											.split("]")[0]));
									lons.add(Double.parseDouble(singleFeature.toJson()
											.split("\"coordinates\":\\[")[1]
											.split(",")[0]));
									data_json.add(singleFeature.toJson());
									i++;
								}
							}
							if (location != null && order.isChecked()) {
								Collections.sort(Places, NumberAwareStringComparator.INSTANCE);
							} else {
								Collections.sort(Places);
							}

							latitudes = new ArrayList<Double>();
							longitudes = new ArrayList<Double>();

							i = 0;
							for (String place : Places) {
								int position = Integer.parseInt(Places.get(i).split("\n")[1]);
								Places.set(i, Places.get(i).split("\n")[0]);
								if (location != null && order.isChecked()) {
									Places.set(i, Places.get(i).split("\r")[1]);
								}
								latitudes.add(lats.get(position));
								longitudes.add(lons.get(position));
								json.add(data_json.get(position));
								i++;
							}
							runOnUiThread(() -> {
								FillList(Places);
							});
						} catch (Exception e) {

						}
					}
				}
			}
		}, 0, 10000);
	}

	@Override
	public void onBackPressed () {
		long currentTime = System.currentTimeMillis();
		if (currentTime - mLastPress > TOAST_DURATION) {
			onBackPressedToast = Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_SHORT);
			onBackPressedToast.show();
			mLastPress = currentTime;
		} else {
			if (onBackPressedToast != null) {
				onBackPressedToast.cancel();  //Difference with previous answer. Prevent continuing showing toast after application exit.
				onBackPressedToast = null;
			}
			Intent homeIntent = new Intent(Intent.ACTION_MAIN);
			homeIntent.addCategory(Intent.CATEGORY_HOME);
			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeIntent);
		}
	}

	@Override
	public void onItemClick (View view, int position) {

		findViewById(R.id.linearLayout).setVisibility(View.GONE);
		findViewById(R.id.center_map).setVisibility(View.VISIBLE);
		findViewById(R.id.compass_map).setVisibility(View.VISIBLE);

		double lat = 0d;
		double lon = 0d;

		if (findViewById(R.id.search_results).getVisibility() == View.GONE) {
			lat = latitudes.get(position);
			lon = longitudes.get(position);
		} else {
			lat = latitudes_search.get(position);
			lon = longitudes_search.get(position);
		}

		CameraPosition location = new CameraPosition.Builder()
				.target(new LatLng(lat, lon
				)) // Sets the new camera position
				.zoom(17) // Sets the zoom
				.build(); // Creates a CameraPosition from the builder
		mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(location), 5000);
	}

	void search () {
		new Thread(() -> {
			ArrayList<String> Places = new ArrayList<>();
			latitudes_search = new ArrayList<>();
			longitudes_search = new ArrayList<>();

			EditText input = (EditText) findViewById(R.id.search);
			String query = input.getText().toString();
			String key = "";
			String value = "";
			if (getResources().getString(R.string.all).contains(query)) {
			} else if (getResources().getString(R.string.training).contains(query)) {
				key = "animal_training";
				value = "dog";
			} else if (getResources().getString(R.string.access).contains(query)) {
				key = "dog";
				value = "yes";
			} else if (getResources().getString(R.string.vet).contains(query)) {
				key = "amenity";
				value = "veterinary";
			} else if (getResources().getString(R.string.grooming).contains(query)) {
				key = "shop";
				value = "pet_grooming";
			} else if (getResources().getString(R.string.shop).contains(query)) {
				key = "shop";
				value = "pet";
			} else if (getResources().getString(R.string.trash).contains(query)) {
				key = "waste";
				value = "dog_excrement";
			} else if (getResources().getString(R.string.museum).contains(query)) {
				key = "tourism";
				value = "museum";
			} else if (getResources().getString(R.string.gallery).contains(query)) {
				key = "tourism";
				value = "gallery";
			} else if (getResources().getString(R.string.archive).contains(query)) {
				key = "amenity";
				value = "archive";
			} else if (getResources().getString(R.string.theatre).contains(query)) {
				key = "amenity";
				value = "theatre";
			} else if (getResources().getString(R.string.bookshop).contains(query)) {
				key = "shop";
				value = "books";
			} else if (getResources().getString(R.string.library).contains(query)) {
				key = "amenity";
				value = "library";
			} else if (getResources().getString(R.string.concerthall).contains(query)) {
				key = "amenity";
				value = "concert_hall";
			} else if (getResources().getString(R.string.accessible).contains(query)) {
				key = "wheelchair";
				value = "yes";
			} else if (getResources().getString(R.string.limited).contains(query)) {
				key = "wheelchair";
				value = "limited";
			} else if (getResources().getString(R.string.designated).contains(query)) {
				key = "wheelchair";
				value = "designated";
			} else if (getResources().getString(R.string.water).contains(query)) {
				key = "amenity";
				value = "drinking_water";
			} else if (getResources().getString(R.string.toilet).contains(query)) {
				key = "amenity";
				value = "toilets";
			} else if (getResources().getString(R.string.vegetarian).contains(query)) {
				key = "diet:vegetarian";
				value = "yes";
			} else if (getResources().getString(R.string.vegan).contains(query)) {
				key = "diet:vegan";
				value = "yes";
			} else if (getResources().getString(R.string.glutenfree).contains(query)) {
				key = "diet:gluten_free";
				value = "yes";
			} else if (getResources().getString(R.string.dairyfree).contains(query)) {
				key = "diet:dairy_free";
				value = "yes";
			} else if (getResources().getString(R.string.lactosefree).contains(query)) {
				key = "diet:lactose_free";
				value = "yes";
			}

			for (int i = 0; i < json.size() - 1; i++) {
				String object = json.get(i);
				try {
					Switch order = findViewById(R.id.order);
					if (mapboxMap.getLocationComponent().getLastKnownLocation() != null) {
						location = Point.fromLngLat(
								mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude(),
								mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude());
						order.setVisibility(View.VISIBLE);
					} else {
						order.setVisibility(View.GONE);
					}

					Feature singleFeature = Feature.fromJson(object);

					//String find = object.split("\"name\":\"")[1].split("\"")[0];
					if (object.toLowerCase().contains(query.toLowerCase()) ||
							object.toLowerCase().contains(key + "\" ?: ?\"" + value)/* ||
                        object.split("\"" + key + "\":\"")[1].split("\"")[0]
                                .contains(value.toLowerCase())*/) {
						String place = "";
						if (location != null && order.isChecked()) {
							double distance = new LatLng(location.latitude(), location.longitude()).distanceTo(
									new LatLng(Double.parseDouble(singleFeature.toJson()
											.split("\"coordinates\":\\[")[1]
											.split(",")[1]
											.split("]")[0]),
											Double.parseDouble(singleFeature.toJson()
													.split("\"coordinates\":\\[")[1]
													.split(",")[0])));
							if (distance > Integer.MAX_VALUE) {
								distance = Integer.MAX_VALUE;
							}
							place += distance;
							place += "\r";
						}
						place += singleFeature.getStringProperty(PROPERTY_NAME);
						if (!Objects.equals(singleFeature.getStringProperty("addr:postcode"), null)) {
							place += " (" + singleFeature.getStringProperty("addr:postcode") + " ";
						}
						if (!Objects.equals(singleFeature.getStringProperty("addr:city"), null)) {
							place += singleFeature.getStringProperty("addr:city");
						}
						if (!Objects.equals(singleFeature.getStringProperty("addr:street"), null)) {
							place += ", " + singleFeature.getStringProperty("addr:street");
							if (!Objects.equals(singleFeature.getStringProperty("addr:housenumber"), null)) {
								place += " " + singleFeature.getStringProperty("addr:housenumber") + ".";
							}
						}
						if (!Objects.equals(singleFeature.getStringProperty("addr:city"), null)) {
							place += ")";
						}
						if (location != null) {
							double distance = Math.round(new LatLng(location.latitude(), location.longitude()).distanceTo(
									new LatLng(Double.parseDouble(singleFeature.toJson()
											.split("\"coordinates\":\\[")[1]
											.split(",")[1]
											.split("]")[0]),
											Double.parseDouble(((singleFeature.toJson()
													.split("\"coordinates\":\\[")[1]
													.split(",")[0]))))));
							if (!(distance > Integer.MAX_VALUE)) {
								place += " (";
								place += Math.round(distance);
								place += " m)";
							}
						}
						latitudes_search.add(Double.parseDouble(object
								.split("\"coordinates\":\\[")[1]
								.split(",")[1]
								.split("]")[0]));
						longitudes_search.add(Double.parseDouble(object
								.split("\"coordinates\":\\[")[1]
								.split(",")[0]));
						if (!Places.contains(place)) {
							Places.add(place);
						}else {
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Switch order = findViewById(R.id.order);
			if (location != null && order.isChecked()) {
				Collections.sort(Places, NumberAwareStringComparator.INSTANCE);
				int i = 0;
				for (String place : Places) {//TODO
					int position = Integer.parseInt(Places.get(i).split("\n")[1]);
					Places.set(i, Places.get(i).split("\n")[0]);
					if (location != null && order.isChecked()) {
						Places.set(i, Places.get(i).split("\r")[1]);
					}
					latitudes_search.add(Double.parseDouble(place
							.split("\"coordinates\":\\[")[1]
							.split(",")[1]
							.split("]")[0]));
					longitudes_search.add(Double.parseDouble(place
							.split("\"coordinates\":\\[")[1]
							.split(",")[0]));
					i++;
				}
			} else {
				Collections.sort(Places);
			}


			// set up the RecyclerView
			RecyclerView recyclerView = findViewById(R.id.search_results);
			recyclerView.setLayoutManager(new LinearLayoutManager(this));
			adapter_search = new Items(this, Places);
			adapter_search.setClickListener(this);
			recyclerView.setAdapter(adapter_search);
			findViewById(R.id.search_results).setVisibility(View.VISIBLE);
			findViewById(R.id.recyclerView).setVisibility(View.GONE);
			findViewById(R.id.center_map).setVisibility(View.VISIBLE);
			findViewById(R.id.compass_map).setVisibility(View.VISIBLE);
			View view = this.getCurrentFocus();
			if (view != null) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}).run();
	}

	@Override
	public void onClick (View v) {
		if (v == findViewById(R.id.search_button)) {
			search();
		} else if (v == findViewById(R.id.close)) {
			findViewById(R.id.info).setVisibility(View.INVISIBLE);
		} else if (v == findViewById(R.id.googlemaps)) {
			Uri gmmIntentUri = Uri.parse("google.navigation:q=" +
					Double.parseDouble(selected_json
							.split("\"coordinates\":\\[")[1]
							.split(",")[1]
							.split("]")[0]) + ", " +
					Double.parseDouble(selected_json
							.split("\"coordinates\":\\[")[1]
							.split(",")[0]));
			Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
			mapIntent.setPackage("com.google.android.apps.maps");
			startActivity(mapIntent);
		} else if (v == findViewById(R.id.search_distance)) {
			new Thread(() -> {
				ArrayList<String> Places = new ArrayList<>();
				ArrayList<String> filter = new ArrayList<>();
				latitudes_search = new ArrayList<>();
				longitudes_search = new ArrayList<>();
				Switch order = findViewById(R.id.order);
				if (mapboxMap.getLocationComponent().getLastKnownLocation() != null) {
					location = Point.fromLngLat(
							mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude(),
							mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude());
					order.setVisibility(View.VISIBLE);
				} else {
					order.setVisibility(View.GONE);
				}

				if (location != null) {
					double query = Double.parseDouble(((EditText) findViewById(R.id.distance)).getText().toString()) * 1000;
					for (int i = 0; i < json.size() - 1; i++) {
						String object = json.get(i);
						try {
							Feature singleFeature = Feature.fromJson(object);

							double distance = new LatLng(location.latitude(), location.longitude()).distanceTo(
									new LatLng(Double.parseDouble(singleFeature.toJson()
											.split("\"coordinates\":\\[")[1]
											.split(",")[1]
											.split("]")[0]),
											Double.parseDouble(singleFeature.toJson()
													.split("\"coordinates\":\\[")[1]
													.split(",")[0])));
							if (distance > Integer.MAX_VALUE) {
								distance = Integer.MAX_VALUE;
							}
							if (distance < query) {
								String place = "";
								place += distance;
								place += "\r";
								place += singleFeature.getStringProperty(PROPERTY_NAME);
								if (!Objects.equals(singleFeature.getStringProperty("addr:postcode"), null)) {
									place += " (" + singleFeature.getStringProperty("addr:postcode") + " ";
								}
								if (!Objects.equals(singleFeature.getStringProperty("addr:city"), null)) {
									place += singleFeature.getStringProperty("addr:city");
								}
								if (!Objects.equals(singleFeature.getStringProperty("addr:street"), null)) {
									place += ", " + singleFeature.getStringProperty("addr:street");
									if (!Objects.equals(singleFeature.getStringProperty("addr:housenumber"), null)) {
										place += " " + singleFeature.getStringProperty("addr:housenumber") + ".";
									}
								}
								if (!Objects.equals(singleFeature.getStringProperty("addr:city"), null)) {
									place += ")";
								}
								distance = Math.round(new LatLng(location.latitude(), location.longitude()).distanceTo(
										new LatLng(Double.parseDouble(singleFeature.toJson()
												.split("\"coordinates\":\\[")[1]
												.split(",")[1]
												.split("]")[0]),
												Double.parseDouble(((singleFeature.toJson()
														.split("\"coordinates\":\\[")[1]
														.split(",")[0]))))));
								if (!(distance > Integer.MAX_VALUE)) {
									place += " (";
									place += Math.round(distance);
									place += " m)";
								}
								latitudes_search.add(Double.parseDouble(object
										.split("\"coordinates\":\\[")[1]
										.split(",")[1]
										.split("]")[0]));
								longitudes_search.add(Double.parseDouble(object
										.split("\"coordinates\":\\[")[1]
										.split(",")[0]));
								if (!Places.contains(place)) {
									Places.add(place);
									filter.add(object);
								}else{
									break;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				if (location != null && order.isChecked()) {
					Collections.sort(Places, NumberAwareStringComparator.INSTANCE);
					int i = 0;
					for (String place : Places) {//TODO
						Places.set(i, Places.get(i).split("\n")[0]);
						if (location != null && order.isChecked()) {
							Places.set(i, Places.get(i).split("\r")[1]);
						}
						latitudes_search.add(Double.parseDouble(filter.get(i)
								.split("\"coordinates\":\\[")[1]
								.split(",")[1]
								.split("]")[0]));
						longitudes_search.add(Double.parseDouble(filter.get(i)
								.split("\"coordinates\":\\[")[1]
								.split(",")[0]));
						i++;
					}
				} else {
					int i = 0;
					for (String place : Places) {
						Places.set(i, Places.get(i).split("\n")[0]);
						Places.set(i, Places.get(i).split("\r")[1]);
						i++;
					}
					Collections.sort(Places);
					i = 0;
					for (String place : Places) {
						latitudes_search.add(Double.parseDouble(filter.get(i)
								.split("\"coordinates\":\\[")[1]
								.split(",")[1]
								.split("]")[0]));
						longitudes_search.add(Double.parseDouble(filter.get(i)
								.split("\"coordinates\":\\[")[1]
								.split(",")[0]));
						i++;
					}
				}


				// set up the RecyclerView
				RecyclerView recyclerView = findViewById(R.id.search_results);
				recyclerView.setLayoutManager(new LinearLayoutManager(this));
				adapter_search = new Items(this, Places);
				adapter_search.setClickListener(this);
				recyclerView.setAdapter(adapter_search);
				findViewById(R.id.search_results).setVisibility(View.VISIBLE);
				findViewById(R.id.recyclerView).setVisibility(View.GONE);
				findViewById(R.id.center_map).setVisibility(View.VISIBLE);
				findViewById(R.id.compass_map).setVisibility(View.VISIBLE);
				View view = this.getCurrentFocus();
				if (view != null) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}).run();
			//TODO
		}
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
		Mapbox.getInstance(this, getString(R.string.access_token));

// This contains the MapView in XML and needs to be called after the access token is configured.
		setContentView(R.layout.activity_main);

		mapView = findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(this);

		BottomNavigationView navView = findViewById(R.id.nav_view);
		navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
		findViewById(R.id.linearLayout).setVisibility(View.GONE);

		Button button = findViewById(R.id.search_button);
		button.setOnClickListener(this);
		Button googlemaps = findViewById(R.id.googlemaps);
		googlemaps.setOnClickListener(this);
		Button distance = findViewById(R.id.search_distance);
		distance.setOnClickListener(this);
		ImageButton close = findViewById(R.id.close);
		close.setOnClickListener(this);
		Spinner spinner = (Spinner) findViewById(R.id.filter);
		spinner.setOnItemSelectedListener(this);

	}

	public void onItemSelected (AdapterView<?> parent, View view,
	                            int pos, long id) {
		String key = "";
		String value = "";
		String s = parent.getSelectedItem().toString();
		if (s.equals(getResources().getString(R.string.all))) {
		} else if (s.equals(getResources().getString(R.string.all))) {
			key = "animal_training";
			value = "dog";
		} else if (s.equals(getResources().getString(R.string.access))) {
			key = "dog";
			value = "yes";
		} else if (s.equals(getResources().getString(R.string.vet))) {
			key = "amenity";
			value = "veterinary";
		} else if (s.equals(getResources().getString(R.string.grooming))) {
			key = "shop";
			value = "pet_grooming";
		} else if (s.equals(getResources().getString(R.string.shop))) {
			key = "shop";
			value = "pet";
		} else if (s.equals(getResources().getString(R.string.trash))) {
			key = "waste";
			value = "dog_excrement";
		} else if (s.equals(getResources().getString(R.string.museum))) {
			key = "tourism";
			value = "museum";
		} else if (s.equals(getResources().getString(R.string.gallery))) {
			key = "tourism";
			value = "gallery";
		} else if (s.equals(getResources().getString(R.string.archive))) {
			key = "amenity";
			value = "archive";
		} else if (s.equals(getResources().getString(R.string.theatre))) {
			key = "amenity";
			value = "theatre";
		} else if (s.equals(getResources().getString(R.string.bookshop))) {
			key = "shop";
			value = "books";
		} else if (s.equals(getResources().getString(R.string.library))) {
			key = "amenity";
			value = "library";
		} else if (s.equals(getResources().getString(R.string.concerthall))) {
			key = "amenity";
			value = "concert_hall";
		} else if (s.equals(getResources().getString(R.string.accessible))) {
			key = "wheelchair";
			value = "yes";
		} else if (s.equals(getResources().getString(R.string.limited))) {
			key = "wheelchair";
			value = "limited";
		} else if (s.equals(getResources().getString(R.string.designated))) {
			key = "wheelchair";
			value = "designated";
		} else if (s.equals(getResources().getString(R.string.water))) {
			key = "amenity";
			value = "drinking_water";
		} else if (s.equals(getResources().getString(R.string.toilet))) {
			key = "amenity";
			value = "toilets";
		} else if (s.equals(getResources().getString(R.string.vegetarian))) {
			key = "diet:vegetarian";
			value = "yes";
		} else if (s.equals(getResources().getString(R.string.vegan))) {
			key = "diet:vegan";
			value = "yes";
		} else if (s.equals(getResources().getString(R.string.glutenfree))) {
			key = "diet:gluten_free";
			value = "yes";
		} else if (s.equals(getResources().getString(R.string.dairyfree))) {
			key = "diet:dairy_free";
			value = "yes";
		} else if (s.equals(getResources().getString(R.string.lactosefree))) {
			key = "diet:lactose_free";
			value = "yes";
		}

		boolean found = false;
		ArrayList<String> Places = new ArrayList<>();
		latitudes_search = new ArrayList<>();
		longitudes_search = new ArrayList<>();

		for (String object : (ArrayList<String>) json.clone()) {
			try {
				String find = object.split("\"" + key + "\":\"")[1].split("\"")[0];

				String tag = object.split("\"" + key + "\":\"")[1].split("\"")[0];
				if (object.split("\"" + key + "\":\"")[1].split("\"")[0]
						.equals(value) ||
						(key.contains("diet:") &&
								object.split("\"" + key + "\":\"")[1].split("\"")[0]
										.equals("only"))) {
					Places.add(object.split("\"name\":\"")[1].split("\"")[0]);
					latitudes_search.add(Double.parseDouble(object
							.split("\"coordinates\":\\[")[1]
							.split(",")[1]
							.split("]")[0]));
					longitudes_search.add(Double.parseDouble(object
							.split("\"coordinates\":\\[")[1]
							.split(",")[0]));
					found = true;
				}
			} catch (Exception e) {
			}
		}
		if (found) {
			// set up the RecyclerView
			RecyclerView recyclerView = findViewById(R.id.search_results);
			recyclerView.setLayoutManager(new LinearLayoutManager(this));
			adapter_search = new Items(this, Places);
			adapter_search.setClickListener(this);
			recyclerView.setAdapter(adapter_search);
			findViewById(R.id.search_results).setVisibility(View.VISIBLE);
			findViewById(R.id.recyclerView).setVisibility(View.GONE);
		}
	}

	public void onNothingSelected (AdapterView<?> parent) {
	}

	public void FillList (ArrayList<String> Places) {
		// set up the RecyclerView
		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		adapter = new Items(this, Places);
		recyclerView.removeAllViewsInLayout();
		//adapter.updateData(Places);
		adapter.setClickListener(this);
		recyclerView.setAdapter(adapter);
	}

	@Override
	public void onMapReady (@NonNull final MapboxMap mapboxMap) {
		this.mapboxMap = mapboxMap;

		String url;
		if (dark) {
			url = getResources().getString(R.string.style_url_dark);
		} else {
			url = getResources().getString(R.string.style_url);
		}

		mapboxMap.setStyle(new Style.Builder().
						fromUri(url),
				style -> {
					enableLocationComponent(style);

					new LoadGeoJsonDataTask(MainActivity.this).execute();
					mapboxMap.addOnMapClickListener(MainActivity.this);

// Map is set up and the style has loaded. Now you can add data or make other map adjustments

					try {
						style.addSource(
								new GeoJsonSource("source-id",
										new URI("http://www.turcsanyivince.hu/maps/" + getResources()
												.getString(R.string.file_name) + "_polygon.geojson")));
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					style.addLayer(new LineLayer("linelayer", "source-id").withProperties(
							PropertyFactory.lineDasharray(new Float[]{0.01f, 2f}),
							PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
							PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
							PropertyFactory.lineWidth(2f),
							PropertyFactory.lineColor(Color.parseColor("#0C7A0E"))
					));

					style.addImage(
							"icon",
							BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.mapbox_marker_icon_default)),
							false
					);
// Add a new source from the GeoJSON data and set the 'cluster' option to true.
					try {
						style.addSource(
								new GeoJsonSource("points",
										new URI("http://www.turcsanyivince.hu/maps/" + getResources()
												.getString(R.string.file_name) + ".geojson"),
										new GeoJsonOptions()
												.withCluster(true)
												.withClusterMaxZoom(14)
												.withClusterRadius(50)
								)
						);
					} catch (URISyntaxException uriSyntaxexception) {
						//Timber.e("Check the URL %s", uriSyntaxException.getMessage());
					}

					addClusteredGeoJsonSource(style);

					addGeoJsonNodeSource(style);
				});
	}

	/**
	 * Initialize the Maps SDK's LocationComponent
	 */
	@SuppressWarnings({"MissingPermission"})
	private void enableLocationComponent (@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
		if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
			LocationComponent locationComponent = mapboxMap.getLocationComponent();

// Set the LocationComponent activation options
			LocationComponentActivationOptions locationComponentActivationOptions =
					LocationComponentActivationOptions.builder(this, loadedMapStyle)
							.useDefaultLocationEngine(false)
							.build();

// Activate with the LocationComponentActivationOptions object
			locationComponent.activateLocationComponent(locationComponentActivationOptions);

// Enable to make component visible
			locationComponent.setLocationComponentEnabled(true);

// Set the component's render mode
			locationComponent.setRenderMode(RenderMode.COMPASS);

			initLocationEngine();
		} else {
			permissionsManager = new PermissionsManager(this);
			permissionsManager.requestLocationPermissions(this);
		}
	}

	/**
	 * Set up the LocationEngine and the parameters for querying the device's location
	 */
	@SuppressLint("MissingPermission")
	private void initLocationEngine () {
		locationEngine = LocationEngineProvider.getBestLocationEngine(this);

		LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
				.setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
				.setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

		locationEngine.requestLocationUpdates(request, callback, getMainLooper());
		locationEngine.getLastLocation(callback);
	}

	@Override
	public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onExplanationNeeded (List<String> permissionsToExplain) {

	}

	@Override
	public void onPermissionResult (boolean granted) {
		if (granted) {
			if (mapboxMap.getStyle() != null) {
				enableLocationComponent(mapboxMap.getStyle());
			}
		} else {
			finish();
		}
	}

	@Override
	protected void onStart () {
		int nightModeFlags =
				mapView.getContext().getResources().getConfiguration().uiMode &
						Configuration.UI_MODE_NIGHT_MASK;
		switch (nightModeFlags) {
			case Configuration.UI_MODE_NIGHT_YES:
				dark = true;
				break;
			default:
				dark = false;
				break;
		}
		super.onStart();
		mapView.onStart();

		FloatingActionButton fab_center = findViewById(R.id.center_map);
		fab_center.setOnClickListener(view -> {
			mapboxMap.getLocationComponent().setCameraMode(CameraMode.NONE);
			mapboxMap.getLocationComponent().setCameraMode(CameraMode.TRACKING,
					1200, 16.0, 0d, 0d, null);
		});
		FloatingActionButton fab_compass = findViewById(R.id.compass_map);
		fab_compass.setOnClickListener(view -> {
			mapboxMap.getLocationComponent().setCameraMode(CameraMode.NONE);
			SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
				mapboxMap.getLocationComponent().setCameraMode(CameraMode.TRACKING_COMPASS,
						1200, 16.0, null, 45.0, null);
			} else {
				mapboxMap.getLocationComponent().setCameraMode(CameraMode.TRACKING_GPS,
						1200, 16.0, null, 45.0, null);
			}
		});

		Switch s = findViewById(R.id.order);

		s.setOnCheckedChangeListener((buttonView, isChecked) ->
				new Thread(() -> {//TODO
					try {
						Switch order = findViewById(R.id.order);
						if (mapboxMap.getLocationComponent().getLastKnownLocation() != null) {
							location = Point.fromLngLat(
									mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude(),
									mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude());
							order.setVisibility(View.VISIBLE);
						} else {
							order.setVisibility(View.GONE);
						}

						FeatureCollection featureCollection = FeatureCollection.fromJson(geoJson);

						ArrayList<String> Places = new ArrayList<>();

						ArrayList<Double> lats = new ArrayList<>();
						ArrayList<Double> lons = new ArrayList<>();
						ArrayList<String> data_json = new ArrayList<>();

						int i = 0;
						for (Feature singleFeature : featureCollection.features()) {
							if (singleFeature.getStringProperty(PROPERTY_NAME) != null) {
								String place = "";
								if (location != null && order.isChecked()) {
									double distance = new LatLng(location.latitude(), location.longitude()).distanceTo(
											new LatLng(Double.parseDouble(singleFeature.toJson()
													.split("\"coordinates\":\\[")[1]
													.split(",")[1]
													.split("]")[0]),
													Double.parseDouble(singleFeature.toJson()
															.split("\"coordinates\":\\[")[1]
															.split(",")[0])));
									if (distance > Integer.MAX_VALUE) {
										distance = Integer.MAX_VALUE;
									}
									place += distance;
									place += "\r";
								}
								place += singleFeature.getStringProperty(PROPERTY_NAME);
								if (!Objects.equals(singleFeature.getStringProperty("addr:postcode"), null)) {
									place += " (" + singleFeature.getStringProperty("addr:postcode") + " ";
								}
								if (!Objects.equals(singleFeature.getStringProperty("addr:city"), null)) {
									place += singleFeature.getStringProperty("addr:city");
								}
								if (!Objects.equals(singleFeature.getStringProperty("addr:street"), null)) {
									place += ", " + singleFeature.getStringProperty("addr:street");
									if (!Objects.equals(singleFeature.getStringProperty("addr:housenumber"), null)) {
										place += " " + singleFeature.getStringProperty("addr:housenumber") + ".";
									}
								}
								if (!Objects.equals(singleFeature.getStringProperty("addr:city"), null)) {
									place += ")";
								}
								if (location != null) {
									double distance = Math.round(new LatLng(location.latitude(), location.longitude()).distanceTo(
											new LatLng(Double.parseDouble(singleFeature.toJson()
													.split("\"coordinates\":\\[")[1]
													.split(",")[1]
													.split("]")[0]),
													Double.parseDouble(((singleFeature.toJson()
															.split("\"coordinates\":\\[")[1]
															.split(",")[0]))))));
									if (!(distance > Integer.MAX_VALUE)) {
										place += " (";
										place += Math.round(distance);
										place += " m)";
									}
								}
								Places.add(place + "\n" + i);


								lats.add(Double.parseDouble(singleFeature.toJson()
										.split("\"coordinates\":\\[")[1]
										.split(",")[1]
										.split("]")[0]));
								lons.add(Double.parseDouble(singleFeature.toJson()
										.split("\"coordinates\":\\[")[1]
										.split(",")[0]));
								data_json.add(singleFeature.toJson());
								i++;
							}
						}
						if (location != null && order.isChecked()) {
							Collections.sort(Places, NumberAwareStringComparator.INSTANCE);
						} else {
							Collections.sort(Places);
						}

						latitudes = new ArrayList<Double>();
						longitudes = new ArrayList<Double>();

						i = 0;
						for (String place : Places) {
							int position = Integer.parseInt(Places.get(i).split("\n")[1]);
							Places.set(i, Places.get(i).split("\n")[0]);
							if (location != null && order.isChecked()) {
								Places.set(i, Places.get(i).split("\r")[1]);
							}
							latitudes.add(lats.get(position));
							longitudes.add(lons.get(position));
							json.add(data_json.get(position));
							i++;
						}

						runOnUiThread(() -> {
							FillList(Places);
						});
					} catch (Exception e) {

					}
				}).run());

		getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(() -> {

			Rect r = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
			int screenHeight = getWindow().getDecorView().getRootView().getHeight();

			int keypadHeight = screenHeight - r.bottom;

			//Log.d(TAG, "keypadHeight = " + keypadHeight);

			if (keypadHeight > screenHeight * 0.15) {
				//Keyboard is opened
				findViewById(R.id.placeholder_keyboard).setVisibility(View.VISIBLE);
				ViewGroup.LayoutParams params;
				Space lower = findViewById(R.id.placeholder_keyboard);
				params = lower.getLayoutParams();
				params.height = keypadHeight;
				lower.setLayoutParams(params);
			} else {
				// keyboard is closed
				findViewById(R.id.placeholder_keyboard).setVisibility(View.GONE);
				hideSystemUI();
			}
		});

		EditText searchView = findViewById(R.id.search);
		searchView.setOnKeyListener((v, keyCode, event) -> {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						search();
						return true;
					default:
						break;
				}
			}
			return false;
		});
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		ViewGroup.LayoutParams params;
		Space upper = findViewById(R.id.placeholder_topbar);
		params = upper.getLayoutParams();
		params.height = result;
		upper.setLayoutParams(params);

	}

	@Override
	protected void onResume () {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onPause () {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onStop () {
		super.onStop();

		if (locationEngine != null) {
			locationEngine.removeLocationUpdates(callback);
		}

		mapView.onStop();
	}

	@Override
	protected void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy () {
		super.onDestroy();
// Prevent leaks
		if (locationEngine != null) {
			locationEngine.removeLocationUpdates(callback);
		}
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory () {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			hideSystemUI();
		}
	}

	private void hideSystemUI () {
		// Enables regular immersive mode.
		// For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
		// Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
						// Set the content to appear under the system bars so that the
						// content doesn't resize when the system bars hide and show.
						| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						// Hide the nav bar and status bar
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_LOW_PROFILE);
	}

	private void addClusteredGeoJsonSource (@NonNull Style loadedMapStyle) {
// Use the GeoJSON source to create three layers: One layer for each cluster category.
// Each point range gets a different fill color.
		int[][] layers = new int[][]{
				new int[]{150, ContextCompat.getColor(this, R.color.colorPrimaryDark)},
				new int[]{20, ContextCompat.getColor(this, R.color.colorPrimary)},
				new int[]{0, ContextCompat.getColor(this, R.color.mapbox_blue)}
		};

		for (int i = 0; i < layers.length; i++) {
//Add clusters' circles
			CircleLayer circles = new CircleLayer("cluster" + "-" + i, "points");
			circles.setProperties(
					circleColor(layers[i][1]),
					circleRadius(18f)
			);

			Expression pointCount = toNumber(get("point_count"));

// Add a filter to the cluster layer that hides the circles based on "point_count"
			circles.setFilter(
					i == 0
							? all(has("point_count"),
							gte(pointCount, literal(layers[i][0]))
					) : all(has("point_count"),
							gte(pointCount, literal(layers[i][0])),
							lt(pointCount, literal(layers[i - 1][0]))
					)
			);
			loadedMapStyle.addLayer(circles);
		}

//Add the count labels
		SymbolLayer count = new SymbolLayer("count", "points");
		count.setProperties(
				textField(Expression.toString(get("point_count"))),
				textSize(12f),
				textColor(Color.WHITE),
				textIgnorePlacement(true),
				textAllowOverlap(true)
		);
		loadedMapStyle.addLayer(count);

	}

	@SuppressLint("WrongConstant")
	private void addGeoJsonNodeSource (@NonNull Style loadedMapStyle) {
//Creating a marker layer for single data points
		SymbolLayer unclustered = new SymbolLayer("unclustered-points",
				"points");

		if (dark) {
		} else {
		}

		int col = Color.parseColor("#000000");
		if (dark) {
			col = Color.parseColor("#FFFFFF");
		}
		unclustered.setProperties(
				iconImage("icon"),
				textField(Expression.switchCase(
						eq(get("amenity"), "veterinary"),
						literal(getResources().getString(R.string.vet)),
						eq(get("amenity"), "theatre"),
						literal(getResources().getString(R.string.theatre)),
						eq(get("amenity"), "archive"),
						literal(getResources().getString(R.string.archive)),
						eq(get("amenity"), "library"),
						literal(getResources().getString(R.string.library)),
						eq(get("amenity"), "concert_hall"),
						literal(getResources().getString(R.string.concerthall)),
						eq(get("amenity"), "drinking_water"),
						literal(getResources().getString(R.string.water)),
						eq(get("amenity"), "toilets"),
						literal(getResources().getString(R.string.toilet)),
						eq(get("amenity"), "waste_basket"),
						literal(getResources().getString(R.string.trash)),
						eq(get("amenity"), "cafe"),
						literal(getResources().getString(R.string.cafe)),
						eq(get("amenity"), "pub"),
						literal(getResources().getString(R.string.pub)),
						eq(get("amenity"), "bar"),
						literal(getResources().getString(R.string.bar)),
						eq(get("amenity"), "restaurant"),
						literal(getResources().getString(R.string.restaurant)),
						eq(get("shop"), "pet_grooming"),
						literal(getResources().getString(R.string.grooming)),
						eq(get("shop"), "books"),
						literal(getResources().getString(R.string.bookshop)),
						eq(get("animal_training"), "dog"),
						literal(getResources().getString(R.string.training)),
						eq(get("leisure"), "dog_park"),
						literal(getResources().getString(R.string.park)),
						eq(get("tourism"), "museum"),
						literal(getResources().getString(R.string.museum)),
						eq(get("tourism"), "gallery"),
						literal(getResources().getString(R.string.gallery)),

						eq(get("amenity"), ""),
						literal(""),
						eq(get("shop"), ""),
						literal(""),
						eq(get("animal_training"), ""),
						literal(""),
						eq(get("leisure"), ""),
						literal(""),
						eq(get("tourism"), ""),
						literal(""),
						literal(getResources().getString(R.string.other)))),
				textSize(18f),
				textColor(Color.BLACK),
				textIgnorePlacement(false),
				textAllowOverlap(false),
				textOffset(new Float[]{0f, -1.5f}),
				textColor(col),
				textAnchor("bottom")
		);

		loadedMapStyle.addLayer(unclustered);
	}

	@Override
	public boolean onMapClick (@NonNull LatLng point) {
		return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
	}

	/**
	 * Sets up all of the sources and layers needed for this example
	 *
	 * @param collection the FeatureCollection to set equal to the globally-declared FeatureCollection
	 */
	public void setUpData (final FeatureCollection collection) {
		featureCollection = collection;
		if (mapboxMap != null) {
			mapboxMap.getStyle(style -> {
				setupSource(style);
                /*setUpImage(style);
                setUpMarkerLayer(style);
                setUpInfoWindowLayer(style);*/
			});
		}
	}

	/**
	 * Adds the GeoJSON source to the map
	 */
	private void setupSource (@NonNull Style loadedStyle) {
		source = new GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection);
		loadedStyle.addSource(source);
	}

	/**
	 * Updates the display of data on the map after the FeatureCollection has been modified
	 */
	private void refreshSource () {
		if (source != null && featureCollection != null) {
			source.setGeoJson(featureCollection);
		}
	}

	/**
	 * Adds the marker image to the map for use as a SymbolLayer icon
	 */
    /*private void setUpImage(@NonNull Style loadedStyle) {
        loadedStyle.addImage(MARKER_IMAGE_ID, BitmapFactory.decodeResource(
                this.getResources(), R.drawable.mapbox_compass_icon));
    }*/

	/**
	 * This method handles click events for SymbolLayer symbols.
	 * <p>
	 * When a SymbolLayer icon is clicked, we moved that feature to the selected state.
	 * </p>
	 *
	 * @param screenPoint the point on screen clicked
	 */
	private boolean handleClickIcon (PointF screenPoint) {
		List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint/*, "unclustered-points"*/);
		if (!features.isEmpty()) {
			for (int j = 0; j < features.size(); j++) {
				try {
					List<Feature> featureList = featureCollection.features();
					for (int i = 0; i < featureList.size(); i++) {
						if (featureList.get(i).id().equals(features.get(0).id())) {
							TextView address = (TextView) findViewById(R.id.address);
							TextView name = (TextView) findViewById(R.id.name);
							TextView note = (TextView) findViewById(R.id.note);
							TextView category = (TextView) findViewById(R.id.category);

							String place = "";
							if (!Objects.equals(featureList.get(i).getStringProperty("addr:city"), null)) {
								place += featureList.get(i).getStringProperty("addr:city");
								if (!Objects.equals(featureList.get(i).getStringProperty("addr:street"), null)) {
									place += ", " + featureList.get(i).getStringProperty("addr:street");
									if (!Objects.equals(featureList.get(i).getStringProperty("addr:housenumber"), null)) {
										place += " " + featureList.get(i).getStringProperty("addr:housenumber") + ".";
									}
								}
							} else {
								List<CarmenFeature> locations =
										new GeocodeTask().execute(featureList.get(i)).get();
								if (locations.isEmpty()) {
								} else {
									place = locations.get(0).placeName().split(", ")[2] + ", " +
											locations.get(0).placeName().split(", ")[1];
								}
							}

							if (featureList.get(i).getStringProperty("name") == null) {
								name.setText("-");
							} else {
								name.setText(featureList.get(i).getStringProperty("name"));
							}
							address.setText(place);
							note.setText(featureList.get(i).getStringProperty("note"));
							String style = "";

							Feature feature = featureList.get(i);
							if (feature.getStringProperty("amenity") != null) {
								switch (feature.getStringProperty("amenity")) {
									case "veterinary":
										style = getResources().getString(R.string.vet);
										break;
									case "theatre":
										style = getResources().getString(R.string.theatre);
										break;
									case "archive":
										style = getResources().getString(R.string.archive);
										break;
									case "library":
										style = getResources().getString(R.string.library);
										break;
									case "concert_hall":
										style = getResources().getString(R.string.concerthall);
										break;
									case "drinking_water":
										style = getResources().getString(R.string.water);
										break;
									case "toilets":
										style = getResources().getString(R.string.toilet);
										break;
									case "waste_basket":
										style = getResources().getString(R.string.trash);
										break;
									case "cafe":
										style = getResources().getString(R.string.cafe);
										break;
									case "pub":
										style = getResources().getString(R.string.pub);
										break;
									case "bar":
										style = getResources().getString(R.string.bar);
										break;
									case "restaurant":
										style = getResources().getString(R.string.restaurant);
										break;
								}
							} else if (feature.getStringProperty("shop") != null) {
								switch (feature.getStringProperty("shop")) {
									case "pet_grooming":
										style = getResources().getString(R.string.grooming);
										break;
									case "books":
										style = getResources().getString(R.string.bookshop);
										break;
									default:
										style = getResources().getString(R.string.shop);
										break;
								}
							} else if (feature.getStringProperty("animal_training") != null) {
								style = getResources().getString(R.string.training);
							} else if (feature.getStringProperty("leisure") != null) {
								switch (feature.getStringProperty("shop")) {
									case "dog_park":
										style = getResources().getString(R.string.park);
										break;
								}
							} else if (feature.getStringProperty("tourism") != null) {
								if (feature.getStringProperty("tourism").equals("museum")) {
									style = getResources().getString(R.string.museum);
								} else if (feature.getStringProperty("tourism").equals("gallery")) {
									style = getResources().getString(R.string.gallery);
								}
							} else if (feature.getStringProperty("healthcare") != null) {
								//TODO: complete
                            /*switch (feature.getStringProperty("healthcare")){
                                case "TODO()":
                                    style = getResources().getString(R.string.TODO());
                                    break;
                            }*/
							} else {
								if (feature.getStringProperty("diet.vegan") != null) {
									style = getResources().getString(R.string.vegan);
								}
								if (feature.getStringProperty("diet.vegetarian") != null) {
									style = getResources().getString(R.string.vegetarian);
								}
								if (feature.getStringProperty("diet.gluten_free") != null) {
									style = getResources().getString(R.string.glutenfree);
								}
								if (feature.getStringProperty("diet.lactose_free") != null) {
									style = getResources().getString(R.string.lactosefree);
								}
								if (feature.getStringProperty("diet.dairy_free") != null) {
									style = getResources().getString(R.string.dairyfree);
								} else {
									style = getResources().getString(R.string.other);
								}
							}

							if (feature.getStringProperty("wheelchair") != null &&
									!feature.getStringProperty("wheelchair").equals("no")) {
								style += " (";
								switch (feature.getStringProperty("wheelchair")) {
									case "yes":
										style += getResources().getString(R.string.accessible);
										break;
									case "designated":
										style += getResources().getString(R.string.designated);
										break;
									case "limited":
										style += getResources().getString(R.string.limited);
										break;
								}
								style += " " +
										getResources().getString(R.string.wheelchair);
							}
							if (feature.getStringProperty("dog") != null &&
									!feature.getStringProperty("dog").equals("no")) {
								if (feature.getStringProperty("wheelchair") != null &&
										!Objects.equals(feature.getStringProperty("wheelchair"), "no")) {
									style += " " + getResources().getString(R.string.and) + " ";
								} else {
									style += " (";
								}
								switch (feature.getStringProperty("dog")) {
									case "yes":
										style += getResources().getString(R.string.accessible);
										break;
									case "leashed":
										style += getResources().getString(R.string.accessible);
										break;
									case "limited":
										style += getResources().getString(R.string.limited);
										break;
								}
								style += " " +
										getResources().getString(R.string.dog);
							}
							if ((feature.getStringProperty("wheelchair") != null &&
									!feature.getStringProperty("wheelchair").equals("no")) ||
									(feature.getStringProperty("dog") != null &&
											!feature.getStringProperty("dog").equals("no"))) {
								style += ")";
							}
							category.setText(style);

							selected_json = featureList.get(i).toJson();

							findViewById(R.id.info).setVisibility(View.VISIBLE);
						}
					}
				} catch (Exception e) {
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Setup a layer with icons.
	 */
    /*private void setUpMarkerLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                        iconImage(MARKER_IMAGE_ID),
                        iconAllowOverlap(true),
                        iconOffset(new Float[]{0f, -8f})
                ));
    }*/

	/**
	 * Setup a layer with Android SDK call-outs
	 * <p>
	 * name of the feature is used as key for the iconImage
	 * </p>
	 */
    /*private void setUpInfoWindowLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
                        .withProperties(
                                *//* show image with id title based on the value of the name feature property *//*
                                iconImage("{id}"),

                                *//* set anchor of icon to bottom-left *//*
                                iconAnchor(ICON_ANCHOR_BOTTOM),

                                *//* all info window and marker image to appear at the same time*//*
                                iconAllowOverlap(true)//,

                                *//* offset the info window to be above the marker *//*
                                //iconOffset(new Float[]{-2f, -28f})
                        ).withFilter(gte(zoom(), 15))
                *//* add a filter to show only when selected feature property is true */
	/*.withFilter(eq((get(PROPERTY_SELECTED)), literal(true)))*//*);
    }*/

	/**
	 * Invoked when the bitmaps have been generated from a view.
	 */
	public void setImageGenResults (HashMap<String, Bitmap> imageMap) {
		if (mapboxMap != null) {
			mapboxMap.getStyle(style -> {
// calling addImages is faster as separate addImage calls for each bitmap.

				try {
					style.addImages(imageMap);
				} catch (Exception exception) {

				}

			});
		}
	}

	/**
	 * Set a feature selected state.
	 *
	 * @param index the index of selected feature
	 */
    /*private void setSelected(int index) {
        Feature feature = featureCollection.features().get(index);
        setFeatureSelectState(feature, true);
        refreshSource();
    }*/

	/**
	 * Selects the state of a feature
	 *
	 * @param feature the feature to be selected.
	 */
    /*private void setFeatureSelectState(Feature feature, boolean selectedState) {
        feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
        refreshSource();
    }*/

	/**
	 * Checks whether a Feature's boolean "selected" property is true or false
	 *
	 * @return true if "selected" is true. False if the boolean property is false.
	 */
    /*private boolean featureSelectStatus(int index) {
        if (featureCollection == null) {
            return false;
        }
        return featureCollection.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
    }*/

	private static class MainActivityLocationCallback
			implements LocationEngineCallback<LocationEngineResult> {

		private final WeakReference<MainActivity> activityWeakReference;

		MainActivityLocationCallback (MainActivity activity) {
			this.activityWeakReference = new WeakReference<>(activity);
		}

		/**
		 * The LocationEngineCallback interface's method which fires when the device's location has changed.
		 *
		 * @param result the LocationEngineResult object which has the last known location within it.
		 */
		@SuppressLint("StringFormatInvalid")
		@Override
		public void onSuccess (LocationEngineResult result) {
			MainActivity activity = activityWeakReference.get();

			if (activity != null) {
				Location location = result.getLastLocation();

				if (location == null) {
					return;
				}

// Pass the new location to the Maps SDK's LocationComponent
				if (activity.mapboxMap != null && result.getLastLocation() != null) {
					activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
				}
			}
		}

		/**
		 * The LocationEngineCallback interface's method which fires when the device's location can not be captured
		 *
		 * @param exception the exception message
		 */
		@Override
		public void onFailure (@NonNull Exception exception) {
			Log.d("LocationChangeActivity", exception.getLocalizedMessage());
			MainActivity activity = activityWeakReference.get();
			if (activity != null) {
			}
		}
	}

	/**
	 * Utility class to generate Bitmaps for Symbol.
	 */
    /*private static class SymbolGenerator {
        static Bitmap generate(@NonNull View view) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(measureSpec, measureSpec);

            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();

            view.layout(0, 0, measuredWidth, measuredHeight);
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }
    }*/

	class GeocodeTask extends AsyncTask<Feature, Void, List<CarmenFeature>> {

		protected List<CarmenFeature> doInBackground (Feature... features) {
            /*MapboxGeocoder client = new MapboxGeocoder.Builder()
                    .setAccessToken(getString(R.string.access_token))
                    .setCoordinates(
                            Float.parseFloat(features[0].toJson()
                                    .split("\"coordinates\":\\[")[1]
                                    .split(",")[0]),

                            Float.parseFloat(features[0].toJson()
                                    .split("\"coordinates\":\\[")[1]
                                    .split(",")[1]
                                    .split("]")[0]))
                    .build();*/
			MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
					.accessToken(getString(R.string.access_token))
					.query(Point.fromLngLat(Double.parseDouble(features[0].toJson()
									.split("\"coordinates\":\\[")[1]
									.split(",")[0]),

							Double.parseDouble(features[0].toJson()
									.split("\"coordinates\":\\[")[1]
									.split(",")[1]
									.split("]")[0])))
					//.geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
					//.languages("hu")
					.build();
			Response<GeocodingResponse> response = null;
			try {
				response = reverseGeocode.executeCall();

			} catch (Exception e) {
				e.printStackTrace();
			}
			List<CarmenFeature> locations = response.body().features();

			return locations;
		}

		protected void onPostExecute (Void result) {

		}
	}

	/**
	 * AsyncTask to load data from the assets folder.
	 */

	private /*static */class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {

		private final WeakReference<MainActivity> activityRef;

		LoadGeoJsonDataTask (MainActivity activity) {
			this.activityRef = new WeakReference<>(activity);
		}

		String loadGeoJsonFromAsset (String filename) {
			StringBuilder sb = new StringBuilder();
			try {
				HttpURLConnection c = (HttpURLConnection) new URL(
						"http://www.turcsanyivince.hu/maps/" + filename + ".geojson").openConnection();
				c.setRequestMethod("GET");
				c.connect();
				InputStream is = c.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String nextLine = "";
				while ((nextLine = reader.readLine()) != null) {
					sb.append(nextLine + "\n");
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return sb.toString();
		}

		@Override
		protected FeatureCollection doInBackground (Void... params) {
			MainActivity activity = activityRef.get();

			if (activity == null) {
				return null;
			}

			geoJson = loadGeoJsonFromAsset(getResources().getString(R.string.file_name));
			return FeatureCollection.fromJson(geoJson);
		}

		private Polygon fromJson (String json) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(Position.class, new PositionDeserializer());
			Type listType = new TypeToken<ArrayList<ArrayList<Position>>>() {
			}.getType();

			List<List<Position>> coordinates = gsonBuilder.create().fromJson(json, listType);

			return Polygon.fromCoordinates(coordinates);
		}

		@Override
		protected void onPostExecute (FeatureCollection featureCollection) {
			super.onPostExecute(featureCollection);
			MainActivity activity = activityRef.get();
			if (featureCollection == null || activity == null) {
				return;
			}

            /*for (Feature singleFeature : featureCollection.features()) {
                singleFeature.addBooleanProperty(PROPERTY_SELECTED, true);
            }*/

			activity.setUpData(featureCollection);
			//new GenerateViewIconTask(activity).execute(featureCollection);
			updateMap();

			//handler.post(update);

		}
	}
}
