package com.anowercs.notespadpro.activities;


import static com.anowercs.notespadpro.AWSAmplify.MyAmplifyApp.S3_REGION;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.anowercs.notespadpro.R;
import com.anowercs.notespadpro.adapters.NotesAdapter;
import com.anowercs.notespadpro.database.DynamoDBHelper;
import com.anowercs.notespadpro.entities.Note;
import com.anowercs.notespadpro.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

//33333333333333333333333333333333333333333333333

import android.app.ProgressDialog;

import android.graphics.drawable.Drawable;

import androidx.appcompat.app.ActionBarDrawerToggle;


import android.view.MenuItem;
import android.widget.FrameLayout;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.drawerlayout.widget.DrawerLayout;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.anowercs.notespadpro.utility.ShareUtils;
import com.anowercs.notespadpro.utility.UserUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements NotesListener {
    private OrientationEventListener orientationEventListener;
    private StaggeredGridLayoutManager layoutManager;

    public static final int REQUEST_CODE_ADD_NOTE =1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private int noteClickedPosition = -1;

    private AlertDialog dialogAddURL;

    /////////// DDDDDDDDDDYYYYYYYYYYYYNNNNNNNNNNAAAAAAAAMMMMMMMOOOOOOOOOOOO
    // Previous constants remain the same

    private DynamoDBHelper dbHelper;
    private DynamoDBMapper dynamoDBMapper;


    //////////// DDDDDDDDDDYYYYYYYYYYYYNNNNNNNNNNAAAAAAAAMMMMMMMOOOOOOOOOOOO

    //#############################@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   NNNNNNNNNNNNNNNNNNNNNNNNNN



    private TextView tv_header_name, TV_signupLink, TV_date;
    private ImageView IV_header_profile, IV_change_profile;
    private ProgressDialog progressDialog;
    private NavigationView drawer_navigation_view;
    EditText inputSearch;


    BottomNavigationView bottomNavigationView;
    DrawerLayout drawerLayout;
    MaterialToolbar material_toolbar;
    FrameLayout frameLayout;

    View headerView;
    ProgressBar headerProgressBar,  loadNotesProgressBar;

    private boolean isNavViewHidden = false;
    private float lastY;
    FrameLayout mainLayout;
    LinearLayout layoutQuickActions;



    private RecyclerView.OnScrollListener recyclerViewScrollListener;
    private static final float HIDE_THRESHOLD = 20;
    private static final int SCROLL_THRESHOLD = 30;
    private static final long SCROLL_DEBOUNCE_TIME = 250L;
    private boolean isScrolling = false;
    private static final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private int currentPage = 0;
    private Handler mainHandler = new Handler(Looper.getMainLooper());





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                Log.e("MainActivity", "Fatal crash: ", throwable);
                throwable.printStackTrace();
            });
        }

        initializeViews();

        actionBarDrawerToggle();
        //bottomNavigationActivity();
        drawerNavigationActivity();
        materialToolbarActivity();
        headerAttributeSet();
        showImageFromS3();


        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);

        // In onCreate, modify your imageAddNoteMain click listener
        imageAddNoteMain.setOnClickListener(v -> {
            currentPage = 0; // Reset pagination
            hasMoreData = true;
            startActivityForResult(
                    new Intent(getApplicationContext(), CreateNoteActivity.class),
                    REQUEST_CODE_ADD_NOTE
            );
        });

        EditText inputSearch = findViewById(R.id.inputSearch);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (notesAdapter != null) {
                    String searchTerm = s.toString();
                    Log.d("MainActivity", "Search input: " + searchTerm);
                    //notesAdapter.cancelTimer();
                    notesAdapter.searchNotes(searchTerm);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // If search is cleared, explicitly refresh the list
                if (s.toString().isEmpty() && notesAdapter != null) {
                    notesAdapter.refreshSourceList();
                }
            }
        });

        findViewById(R.id.imageAddNote).setOnClickListener(v -> startActivityForResult(
                new Intent(getApplicationContext(),CreateNoteActivity.class),
                REQUEST_CODE_ADD_NOTE
        ));

        // imageAddImage
        //imageAddNote

        findViewById(R.id.imageAddImage).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
            }
        });

        findViewById(R.id.imageAddWebLink).setOnClickListener(v -> showAddURLDialog());

       // deviceOrientation();
        showLoadingbeforeNotes();

    }



    private void deviceOrientation() {
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;
                }

                // Detect orientation and adjust grid layout
                if (orientation >= 315 || orientation < 45) {
                    // Portrait
                    layoutManager.setSpanCount(2);
                    Log.d("Orientation", "Portrait - 2 columns");
                } else if (orientation >= 135 && orientation < 225) {
                    // Reverse Portrait
                    layoutManager.setSpanCount(2);
                    Log.d("Orientation", "Reverse Portrait - 2 columns");
                } else if (orientation >= 45 && orientation < 135) {
                    // Landscape
                    layoutManager.setSpanCount(3);
                    Log.d("Orientation", "Landscape - 3 columns");
                } else if (orientation >= 225 && orientation < 315) {
                    // Reverse Landscape
                    layoutManager.setSpanCount(3);
                    Log.d("Orientation", "Reverse Landscape - 3 columns");
                }
            }
        };

        // Enable the listener
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
            Log.d("Orientation", "Orientation listener enabled");
        } else {
            Log.e("Orientation", "Cannot detect orientation");
        }
    }


    private void materialToolbarActivity() {
        // Set up toolbar menu item click listener
        material_toolbar.setOnMenuItemClickListener(new MaterialToolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.exitApp) {
                    showShutdownConfirmation();
                    return true;
                }
                return false;
            }
        });
    }
    private void showShutdownConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit App")
                .setMessage("Are you sure you want to exit the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performShutdown();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void performShutdown() {
        // Show a progress dialog while shutting down
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Closing app...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create a handler to add a small delay for visual feedback
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Clear the back stack and close all activities
                finishAffinity();

                // Force close the app
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }, 1000); // 1 second delay
    }

    private void showImageFromS3() {

        UserUtils.fetchUserDetails(this, new UserUtils.OnUserDetailsFetchedListener() {
            @Override
            public void onUserDetailsFetched(Map<String, String> attributes) {
                // Handle the fetched user details
                attributes.get("name");

                String imageUrl = attributes.get("picture");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    runOnUiThread(() -> {
                        headerProgressBar.setVisibility(View.VISIBLE);
                        Glide.with( MainActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.demo_profile_3) // Default image
                                .circleCrop()
                                .signature(new ObjectKey(System.currentTimeMillis()))
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        headerProgressBar.setVisibility(View.GONE); // Hide ProgressBar on error

                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        headerProgressBar.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .into(IV_header_profile);
                    });
                }

            }

            @Override
            public void onError(Exception exception) {
                // Handle the error
                exception.printStackTrace();
            }
        });

    }
    private void headerAttributeSet() {
        // Call the fetchUserDetails method from the utility class
        UserUtils.fetchUserDetails(this, new UserUtils.OnUserDetailsFetchedListener() {
            @Override
            public void onUserDetailsFetched(Map<String, String> attributes) {
                // Handle the fetched user details
                tv_header_name.setText(attributes.get("name"));

                }

            @Override
            public void onError(Exception exception) {
                // Handle the error
                exception.printStackTrace();
            }
        });
    }
    private void drawerNavigationActivity() {

        drawer_navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Close the drawer
                drawerLayout.closeDrawers();

                int id = item.getItemId(); // Get the selected item ID
                UserUtils.fetchUserDetails(MainActivity.this, new UserUtils.OnUserDetailsFetchedListener() {
                    @Override
                    public void onUserDetailsFetched(Map<String, String> attributes) {
                        TV_date.setText(attributes.get("birthdate"));

                    }

                    @Override
                    public void onError(Exception exception) {
                        // Handle error
                    }
                });



                // Handle navigation based on the selected item
                if (id == R.id.profile) {
                    drawerLayout.post(() -> {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        
                    });

                } else if (id == R.id.settings) {
                    drawerLayout.post(() -> {
                        Intent intent = new Intent(MainActivity.this, ChangePasswordActivity.class);
                        startActivity(intent);
                    });

                    
                }else if (id == R.id.shareApp) {
                    String contentToShare = "Check out this awesome app!"; // Your content here
                    ShareUtils.shareContent(MainActivity.this, contentToShare, "Share via");


                }
                else if (id == R.id.usersFitness) {
                    drawerLayout.post(() -> {
                        Intent intent = new Intent(MainActivity.this, FitnessActivity.class);
                        startActivity(intent);
                    });
                }
                else if (id == R.id.logout) {
                    AWSMobileClient.getInstance().signOut();
                    Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }

                return true;
            }
        });
    }

   /* private void bottomNavigationActivity() {

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId() == R.id.home) {
                    Toast.makeText(MainActivity.this, "This is Home", Toast.LENGTH_SHORT).show();
                }
                else if(item.getItemId() == R.id.anchor){


                    Toast.makeText(getApplicationContext(), "This is Anchor", Toast.LENGTH_SHORT).show();
                    bottomNavigationView.getOrCreateBadge(R.id.anchor).clearNumber();
                }
                else if(item.getItemId() == R.id.altrout){
                    Toast.makeText(MainActivity.this, "This is Alt rout", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });
    }*/

    private void actionBarDrawerToggle() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                MainActivity.this, drawerLayout, material_toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initializeViews() {


        //bottomNavigationView = findViewById(R.id.bottomNavigationView);

        loadNotesProgressBar = findViewById(R.id.loadNotesProgressBar);

        drawerLayout = findViewById(R.id.drawer_layout);
        material_toolbar = findViewById(R.id.material_toolbar);
        frameLayout = findViewById(R.id.frame_layout);
        drawer_navigation_view = findViewById(R.id.drawer_navigation_view);

        headerView = drawer_navigation_view.getHeaderView(0);
        //setSupportActionBar(material_toolbar);

        // All Text View
        tv_header_name = headerView.findViewById(R.id.tv_header_name);
        TV_signupLink = headerView.findViewById(R.id.TV_signupLink);

        TV_date = headerView.findViewById(R.id.TV_date);
        IV_header_profile = headerView.findViewById(R.id.IV_header_profile);
        //IV_change_profile = headerView.findViewById(R.id.IV_change_profile);
        headerProgressBar = headerView.findViewById(R.id.headerProgressBar);
        setupDynamoDB();

        // In initializeViews()
        //loadNotes(REQUEST_CODE_SHOW_NOTES, false);

        initializeRecyclerView();

        // Then load the notes
        //loadNotes(REQUEST_CODE_SHOW_NOTES, false);

        // Find the ConstraintLayout
        mainLayout = findViewById(R.id.frame_layout);

        // Add touch listener
        layoutQuickActions = findViewById(R.id.layoutQuickActions);
        inputSearch = findViewById(R.id.inputSearch);

    }


    private void initializeRecyclerView() {
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        if (notesRecyclerView != null) {
            Log.d("MainActivity", "Initializing RecyclerView");

            // Basic setup
            StaggeredGridLayoutManager layoutManager =
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            notesRecyclerView.setLayoutManager(layoutManager);

            // Initialize list and adapter
            noteList = new ArrayList<>();
            notesAdapter = new NotesAdapter(noteList, this);
            notesRecyclerView.setAdapter(notesAdapter);

            // Add this line to setup scroll listener
            setupScrollListener();

            // Reset pagination and load initial notes
            currentPage = 0;
            hasMoreData = true;
            Log.d("MainActivity", "Loading initial notes");
            loadNotes(REQUEST_CODE_SHOW_NOTES, false);
        } else {
            Log.e("MainActivity", "RecyclerView not found!");
        }
    }


    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    // Notes
    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    private void selectImage(){

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }


    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                if (shouldShowRequestPermissionRationale(permissions[0])) {
                    Toast.makeText(this, "Storage permission is required to select images", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with selecting the image
                selectImage();
            } else {
                // If permission was denied
                if (shouldShowRequestPermissionRationale(permissions[0])) {
                    // Inform the user about the permission necessity
                    Toast.makeText(this, "Storage permission is required to select images", Toast.LENGTH_SHORT).show();
                } else {
                    // If the user selected 'Don't ask again', handle accordingly
                    Toast.makeText(this, "Storage permission is permanently denied. Please enable it in settings.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void setupDynamoDB() {

        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance());
        dynamoDBClient.setRegion(com.amazonaws.regions.Region.getRegion(S3_REGION));  // ✅ Set region explicitly

        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .build();
        Log.d("DynamoDBHelper", "DynamoDBMapper initialized");

        this.dbHelper = new DynamoDBHelper(dynamoDBMapper);
    }

    private void loadNotes(final int requestCode, final boolean isNoteDeleted) {
        if (isLoading) return;
        isLoading = true;

        // Show loading indicator
        showLoadingIndicator();

        AsyncTask.execute(() -> {
            try {
                if (dbHelper == null || AWSMobileClient.getInstance() == null) {
                    mainHandler.post(() -> {
                        hideLoadingIndicator();
                        isLoading = false;
                    });
                    return;
                }

                String userId = AWSMobileClient.getInstance().getIdentityId();
                if (userId == null) {
                    mainHandler.post(() -> {
                        hideLoadingIndicator();
                        isLoading = false;
                    });
                    return;
                }

                // Get paginated notes
                int offset = currentPage * PAGE_SIZE;
                final List<Note> notes = dbHelper.getPaginatedNotes(userId, offset, PAGE_SIZE);
                Log.d("MainActivity", "Loaded notes: " + notes.size() + " for page: " + currentPage);

                mainHandler.post(() -> {
                    try {
                        if (isFinishing() || noteList == null || notesAdapter == null) {
                            return;
                        }

                        hideLoadingbeforeNotes();

                        // Clear list only on first page or refresh
                        if (currentPage == 0) {
                            noteList.clear();
                        }

                        // Add new notes and update UI
                        if (notes != null && !notes.isEmpty()) {
                            noteList.addAll(notes);

                            notesAdapter.notifyDataSetChanged();
                            // Update source data in adapter
                            // Update source data in adapter for search functionality
                            notesAdapter.updateSourceData(noteList);

                            // Update pagination state
                            hasMoreData = notes.size() == PAGE_SIZE;
                            if (hasMoreData) {
                                currentPage++;
                            }
                        } else {
                            hasMoreData = false;
                        }

                        // Scroll to top if adding new note
                        if (requestCode == REQUEST_CODE_ADD_NOTE && !noteList.isEmpty()) {
                            notesRecyclerView.smoothScrollToPosition(0);
                        }

                    } catch (Exception e) {
                        Log.e("MainActivity", "Error updating UI: " + e.getMessage());
                    } finally {
                        isLoading = false;
                        hideLoadingIndicator();
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    Log.e("MainActivity", "Error loading notes: " + e.getMessage());
                    Toast.makeText(MainActivity.this,
                            "Error loading notes: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    isLoading = false;
                    hideLoadingIndicator();
                });
            }
        });
    }


    @Override
    public void onNoteCLicked(Note note, int position) {
        if (note != null) {
            noteClickedPosition = position;
            Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
            intent.putExtra("isViewOrUpdate", true);
            intent.putExtra("note", note);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
        }
    }
   // delete notes

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            loadNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                boolean isNoteDeleted = data.getBooleanExtra("isNoteDeleted", false);
                Log.d("MainActivity", "Note deletion status: " + isNoteDeleted);
                if (isNoteDeleted) {
                    // Note was deleted, refresh the list
                    noteList.remove(noteClickedPosition);
                    notesAdapter.notifyItemRemoved(noteClickedPosition);
                    refreshNotes(); // Add this
                    loadNotes(REQUEST_CODE_UPDATE_NOTE, true);
                } else {
                    // Note was updated
                    refreshNotes(); // Add this
                    loadNotes(REQUEST_CODE_UPDATE_NOTE, false);

                }
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try {
                        Log.d("SelectedImageURI", selectedImageUri.toString());
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imageUri", selectedImageUri.toString());
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    //##################################################################################################################################
    private void showAddURLDialog(){
        if(dialogAddURL == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if(dialogAddURL.getWindow() !=null){
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(v -> {
                if(inputURL.getText().toString().trim().isEmpty()){
                    Toast.makeText(MainActivity.this,"Enter URL",Toast.LENGTH_SHORT).show();

                }else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                    Toast.makeText(MainActivity.this,"Enter Valid URL",Toast.LENGTH_SHORT).show();
                }else {
                    dialogAddURL.dismiss();
                    Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                    intent.putExtra("isFromQuickActions",true);
                    intent.putExtra("quickActionType","URL");
                    intent.putExtra("URL",inputURL.getText().toString());
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);

                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogAddURL.dismiss());

        }
        dialogAddURL.show();

    }

    private void refreshNotes() {
        currentPage = 0;
        hasMoreData = true;
        loadNotes(REQUEST_CODE_SHOW_NOTES, false);
    }

    // Replace your existing setupScrollListener method with this
    // Optimize your scroll listener
    private void setupScrollListener() {
        final Handler scrollHandler = new Handler(Looper.getMainLooper());
        final long SCROLL_THROTTLE = 250L; // Increase throttle time
        Runnable scrollRunnable = null;

        recyclerViewScrollListener = new RecyclerView.OnScrollListener() {
            private boolean isLoadingMore = false;
            private long lastLoadTime = 0;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                try {
                    // Throttle scroll events
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastLoadTime < SCROLL_THROTTLE) {
                        return;
                    }

                    // Handle quick actions visibility with less frequency
                    if (Math.abs(dy) > SCROLL_THRESHOLD * 2) { // Increased threshold
                        if (layoutQuickActions != null) {
                            layoutQuickActions.setVisibility(dy > 0 ? View.GONE : View.VISIBLE);
                        }
                    }

                    // Load more only if we're not already loading and have more data
                    if (!isLoading && hasMoreData && !isLoadingMore) {
                        StaggeredGridLayoutManager layoutManager =
                                (StaggeredGridLayoutManager) recyclerView.getLayoutManager();

                        if (layoutManager != null) {
                            int[] lastVisiblePositions = new int[layoutManager.getSpanCount()];
                            layoutManager.findLastVisibleItemPositions(lastVisiblePositions);

                            int maxPosition = 0;
                            for (int position : lastVisiblePositions) {
                                maxPosition = Math.max(maxPosition, position);
                            }

                            if (maxPosition >= noteList.size() - 5) {
                                isLoadingMore = true;
                                lastLoadTime = currentTime;

                                // Load more with delay
                                scrollHandler.postDelayed(() -> {
                                    loadNotes(REQUEST_CODE_SHOW_NOTES, false);
                                    isLoadingMore = false;
                                }, 100);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Scroll error: " + e.getMessage());
                }
            }
        };

        if (notesRecyclerView != null) {
            notesRecyclerView.clearOnScrollListeners();
            notesRecyclerView.addOnScrollListener(recyclerViewScrollListener);
        }
    }

    private void optimizeMemory() {
        // Lower image quality in RecyclerView when scrolling
        notesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Full quality when stopped
                    Glide.with(MainActivity.this).resumeRequests();
                } else {
                    // Lower quality when scrolling
                    Glide.with(MainActivity.this).pauseRequests();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        optimizeMemory();
    }


    @Override
    protected void onDestroy() {
        try {

            if (orientationEventListener != null) {
                orientationEventListener.disable();
            }

            // Remove scroll listener and clear recyclerView
            if (notesRecyclerView != null) {
                notesRecyclerView.clearOnScrollListeners();
                notesRecyclerView.setAdapter(null);
            }

            // Clear adapter and list data
            if (notesAdapter != null) {
                notesAdapter.cancelTimer(); // Only keep if your adapter has this method
            }

            if (noteList != null) {
                noteList.clear();
            }

            // Safely dismiss dialog
            if (dialogAddURL != null && dialogAddURL.isShowing()) {
                try {
                    dialogAddURL.dismiss();
                } catch (IllegalArgumentException e) {
                    Log.w("MainActivity", "Error dismissing dialog: " + e.getMessage());
                }
            }

            // Clean up references
            recyclerViewScrollListener = null;
            notesAdapter = null;
            noteList = null;
            dialogAddURL = null;

        } catch (Exception e) {
            Log.e("MainActivity", "Error in onDestroy: " + e.getMessage());
        } finally {
            super.onDestroy();
        }
    }

    private void showLoadingIndicator() {
        // Add a ProgressBar to your layout and show it here
        if (headerProgressBar != null) {
            mainHandler.post(() -> headerProgressBar.setVisibility(View.VISIBLE));
        }
    }

    private void hideLoadingIndicator() {
        if (headerProgressBar != null) {
            mainHandler.post(() -> headerProgressBar.setVisibility(View.GONE));
        }
    }

    private void showLoadingbeforeNotes() {
        loadNotesProgressBar.setVisibility(View.VISIBLE);
        notesRecyclerView.setVisibility(View.GONE);
    }

    private void hideLoadingbeforeNotes() {
        loadNotesProgressBar.setVisibility(View.GONE);
        notesRecyclerView.setVisibility(View.VISIBLE);
    }


}

