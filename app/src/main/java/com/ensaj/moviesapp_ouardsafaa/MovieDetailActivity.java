package com.ensaj.moviesapp_ouardsafaa;

import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MovieDetailActivity";
    private static final String TMDB_API_KEY = "877b99fe73aca6378b8e3b8d95d36199";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private SupportMapFragment mapFragment;
    private TextView descriptionTextView;
    private TextView nameTextView;
    private ImageView img;
    private String trailerKey;
    private RequestQueue requestQueue;
    private Button playButton;
    private GoogleMap mMap;

    // Coordonnées du cinéma (Casablanca)
    private static final LatLng CINEMA_LOCATION = new LatLng(33.596460, -7.615480);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        descriptionTextView = findViewById(R.id.Details);
        img = findViewById(R.id.imageview);
        nameTextView = findViewById(R.id.textName);
        playButton = findViewById(R.id.playButton);
        requestQueue = Volley.newRequestQueue(this);

        int movieId = getIntent().getIntExtra("movieId", -1);
        if (movieId != -1) {
            fetchMovieDetails(movieId);
        } else {
            descriptionTextView.setText("Aucun film trouvé");
        }

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTrailer();
            }
        });

        // Initialiser la carte
        mapFragment = (SupportMapFragment)
              getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
           mapFragment.getMapAsync(this);
        }
    }

    private void fetchMovieDetails(int movieId) {
        String movieDetailsUrl = "https://api.themoviedb.org/3/movie/"
                + movieId + "?api_key=" + TMDB_API_KEY;
        String movieVideosUrl = "https://api.themoviedb.org/3/movie/"
                + movieId + "/videos?api_key=" + TMDB_API_KEY;

        // Requête détails du film
        JsonObjectRequest movieDetailsRequest = new JsonObjectRequest(
                Request.Method.GET, movieDetailsUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String movieName = response.getString("title");
                            String movieDescription = response.getString("overview");
                            String imageUrl = "https://image.tmdb.org/t/p/w500"
                                    + response.getString("poster_path");
                            nameTextView.setText(movieName);
                            descriptionTextView.setText(movieDescription);
                            Glide.with(MovieDetailActivity.this)
                                    .load(imageUrl)
                                    .into(img);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSON error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching movie details: " + error.getMessage());
                        descriptionTextView.setText("Erreur de chargement");
                    }
                });

        // Requête vidéos/trailer
        JsonObjectRequest movieVideosRequest = new JsonObjectRequest(
                Request.Method.GET, movieVideosUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("results")) {
                                JSONArray results = response.getJSONArray("results");
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject video = results.getJSONObject(i);
                                    if (video.getString("type").equals("Trailer")
                                            && video.getString("site").equals("YouTube")) {
                                        trailerKey = video.getString("key");
                                        Log.d(TAG, "Trailer Key: " + trailerKey);
                                        break;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching videos: " + error.getMessage());
                    }
                });

        requestQueue.add(movieDetailsRequest);
        requestQueue.add(movieVideosRequest);
    }

    private void playTrailer() {
        if (trailerKey != null && !trailerKey.isEmpty()) {
            String trailerUrl = "https://www.youtube.com/embed/" + trailerKey;
            Intent intent = new Intent(MovieDetailActivity.this, VideoPlayer.class);
            intent.putExtra("videoUrl", trailerUrl);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Trailer non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Ajouter le marqueur du cinéma
        mMap.addMarker(new MarkerOptions()
                .position(CINEMA_LOCATION)
                .title("Cinéma")
                .snippet("Venez voir ce film ici !"));

        // Vérifier permission localisation
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            moveToCurrentLocation();
        } else {
            // Centrer sur le cinéma par défaut
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CINEMA_LOCATION, 14));
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                LatLng currentLocation =
                        new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
            } else {
                // Centrer sur le cinéma si localisation indisponible
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CINEMA_LOCATION, 14));
                Toast.makeText(this, "Position actuelle non disponible",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CINEMA_LOCATION, 14));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    try {
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    moveToCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Permission localisation refusée",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}