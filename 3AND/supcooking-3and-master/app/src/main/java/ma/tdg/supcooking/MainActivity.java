package ma.tdg.supcooking;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import ma.tdg.supcooking.model.Recipe;
import ma.tdg.supcooking.model.User;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecipeAsyncTask mLastTask;
    private ProgressBar mProgressBar;
    private TextView mNoRecipes;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecipeListAdapter mAdapter;

    private boolean mIsOnUserRecipeTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecipeEditActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        mIsOnUserRecipeTab = false;

        View headerView = mNavigationView.getHeaderView(0);
        TextView navFullName = headerView.findViewById(R.id.user_full_name);
        navFullName.setText(User.getProfile().getFullName());
        TextView navEmail =  headerView.findViewById(R.id.user_email);
        navEmail.setText(User.getProfile().getEmail());

        mProgressBar = findViewById(R.id.recipe_progress);
        mNoRecipes = findViewById(R.id.recipe_empty);
        mRecyclerView = findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(llm);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mLastTask == null || mLastTask.getStatus() == AsyncTask.Status.FINISHED) {
                    mLastTask = new RecipeAsyncTask(RecipeTaskAction.SHOW_UPDATED_TRENDING_RECIPES);
                    mLastTask.execute();
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        Recipe.initDataRepo(getApplication());
        mLastTask = new RecipeAsyncTask(RecipeTaskAction.SHOW_UPDATED_TRENDING_RECIPES);
        mLastTask.execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_list, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSwipeRefreshLayout.setEnabled(false);
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (!mIsOnUserRecipeTab) {
                    mSwipeRefreshLayout.setEnabled(true);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_trending_recipes:
                if (mIsOnUserRecipeTab) {
                    mIsOnUserRecipeTab = false;
                    // only one task at a time (prevent possible race conditions/memory leaks)
                    if (mLastTask == null || mLastTask.getStatus() == AsyncTask.Status.FINISHED) {
                        mLastTask = new RecipeAsyncTask(RecipeTaskAction.SHOW_TRENDING_RECIPES);
                        mLastTask.execute();
                    }
                }
                break;
            case R.id.nav_user_recipes:
                if (!mIsOnUserRecipeTab) {
                    mIsOnUserRecipeTab = true;
                    if (mLastTask == null || mLastTask.getStatus() == AsyncTask.Status.FINISHED) {
                        mLastTask = new RecipeAsyncTask(RecipeTaskAction.SHOW_USER_RECIPES);
                        mLastTask.execute();
                    }
                }
                break;
            case R.id.nav_profile:
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Logout")
                        .setMessage("Are you sure?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("user_profile", null).apply();
                                User.doLogout();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create()
                        .show();
                break;
            case R.id.nav_about:
                intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsOnUserRecipeTab) {
            if (mLastTask == null || mLastTask.getStatus() == AsyncTask.Status.FINISHED) {
                mLastTask = new RecipeAsyncTask(RecipeTaskAction.SHOW_USER_RECIPES);
                mLastTask.execute();
            }
        }
    }

    public enum RecipeTaskAction {
        SHOW_UPDATED_TRENDING_RECIPES,
        SHOW_TRENDING_RECIPES,
        SHOW_USER_RECIPES,
    }

    @SuppressLint("StaticFieldLeak")
    public class RecipeAsyncTask extends AsyncTask<Void, Void, List> {

        private RecipeTaskAction mRecipeTaskAction;
        private String mErrorMessage = null;

        RecipeAsyncTask(RecipeTaskAction taskAction) {
            super();
            mRecipeTaskAction = taskAction;
            switch (mRecipeTaskAction) {
                case SHOW_UPDATED_TRENDING_RECIPES:
                case SHOW_TRENDING_RECIPES:
                    Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.title_view_recipe);
                    mFab.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setEnabled(true);
                    break;
                case SHOW_USER_RECIPES:
                    Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.title_view_user_recipe);
                    break;
            }
            mNoRecipes.setVisibility(View.GONE);

            mRecyclerView.setAdapter(null);

            if (!mSwipeRefreshLayout.isRefreshing()) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List doInBackground(Void... params) {
            switch (mRecipeTaskAction) {
                case SHOW_UPDATED_TRENDING_RECIPES:
                    try {
                        return Recipe.getRecipes(true);
                    } catch (Exception e) {
                        mErrorMessage = e.getMessage();
                        return null;
                    }
                case SHOW_TRENDING_RECIPES:
                    try {
                        return Recipe.getRecipes();
                    } catch (Exception e) {
                        mErrorMessage = e.getMessage();
                        return null;
                    }
                case SHOW_USER_RECIPES:
                    try {
                        return Recipe.getUserRecipes();
                    } catch (Exception e) {
                        mErrorMessage = e.getMessage();
                        return null;
                    }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List recipes) {
            switch (mRecipeTaskAction) {
                case SHOW_USER_RECIPES:
                    mFab.setVisibility(View.VISIBLE);
                    mSwipeRefreshLayout.setEnabled(false);
                    break;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mProgressBar.setVisibility(View.GONE);
            if (recipes == null || recipes.size() == 0) {
                mNoRecipes.setVisibility(View.VISIBLE);
            }
            if (recipes == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Error")
                        .setMessage(mErrorMessage == null ? "An unknown error happened while trying to list recipes" : mErrorMessage)
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
                        .show();
                return;
            }
            mAdapter = new RecipeListAdapter(recipes, mIsOnUserRecipeTab);
            mRecyclerView.setAdapter(mAdapter);
        }
    }
}
