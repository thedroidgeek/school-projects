package ma.tdg.supcooking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import ma.tdg.supcooking.model.Recipe;
import ma.tdg.supcooking.model.UserRecipe;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class RecipeEditActivity extends AppCompatActivity {

    private EditText mRecipeName;
    private EditText mRecipeType;
    private EditText mRecipePrepTime;
    private EditText mRecipeCookingTime;
    private EditText mRecipeIngredients;
    private EditText mRecipePrepSteps;
    private TextView mRecipeChosenPicture;
    private ImageButton mRemovePictureButton;

    private String mRecipePicturePath;
    private Recipe mRecipe;

    private boolean mHasFormChanged;

    private static final int IMAGE_PICKER_PERMISSION_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_edit);

        mRecipeName = findViewById(R.id.edit_recipe_name);
        mRecipeType = findViewById(R.id.edit_recipe_type);
        mRecipePrepTime = findViewById(R.id.edit_recipe_prep_time);
        mRecipeCookingTime = findViewById(R.id.edit_recipe_cooking_time);
        mRecipeIngredients = findViewById(R.id.edit_recipe_ingredients);
        mRecipePrepSteps = findViewById(R.id.edit_recipe_prep_steps);
        mRecipeChosenPicture = findViewById(R.id.edit_recipe_selected_picture);
        mRemovePictureButton = findViewById(R.id.edit_recipe_remove_picture_button);

        ImageButton mAddPictureButton = findViewById(R.id.edit_recipe_add_picture_button);
        mAddPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(RecipeEditActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions(RecipeEditActivity.this,
                            new String[]{Manifest.permission.CAMERA}, IMAGE_PICKER_PERMISSION_REQUEST);
                } else {
                    EasyImage.openChooserWithGallery(RecipeEditActivity.this, "Pick a picture for your recipe", 0);
                }
            }
        });

        mRemovePictureButton.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void onClick(View view) {
                File f = new File(mRecipePicturePath.substring(7));
                if (f.exists()) {
                    f.delete();
                }
                mRecipePicturePath = null;
                mHasFormChanged = true;
                mRecipeChosenPicture.setText(getString(R.string.label_recipe_picture_not_selected));
                mRemovePictureButton.setVisibility(View.GONE);
            }
        });

        setTitle("Add Recipe");

        Bundle extras = getIntent().getExtras();

        if (extras != null && extras.containsKey("recipe")) {
            mRecipe = (Recipe) getIntent().getSerializableExtra("recipe");
            if (mRecipe != null) {
                setTitle("Edit Recipe");
                mRecipeName.setText(mRecipe.getName());
                mRecipeType.setText(mRecipe.getType());
                mRecipeCookingTime.setText(String.valueOf(mRecipe.getCookingTime()));
                mRecipePrepTime.setText(String.valueOf(mRecipe.getPreparationTime()));
                mRecipeIngredients.setText(mRecipe.getIngredients());
                mRecipePrepSteps.setText(mRecipe.getPreparationSteps());
                mRecipePicturePath = mRecipe.getPictureLocation();
                if (mRecipePicturePath != null) {
                    mRecipeChosenPicture.setText(R.string.label_recipe_picture_selected);
                    mRemovePictureButton.setVisibility(View.VISIBLE);
                }
            }
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mHasFormChanged = true;
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) { }
        };

        mRecipeName.addTextChangedListener(watcher);
        mRecipeType.addTextChangedListener(watcher);
        mRecipePrepTime.addTextChangedListener(watcher);
        mRecipeCookingTime.addTextChangedListener(watcher);
        mRecipeIngredients.addTextChangedListener(watcher);
        mRecipePrepSteps.addTextChangedListener(watcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.delete_button).setVisible(mRecipe != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.save_button:
                applyRecipeChanges();
                break;
            case R.id.delete_button:
                deleteRecipe();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                mRecipeChosenPicture.setText(R.string.label_recipe_picture_selected);
                mRemovePictureButton.setVisibility(View.VISIBLE);
                if (mRecipePicturePath != null) {
                    File f = new File(mRecipePicturePath.substring(7));
                    if (f.exists()) {
                        f.delete();
                    }
                }
                mRecipePicturePath = "file://" + imageFile.getPath();
                mHasFormChanged = true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mHasFormChanged) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning")
                    .setMessage("Are you sure you want to go back?\nUnsaved changes will be lost")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create()
                    .show();
        } else {
            finish();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void applyRecipeChanges() {
        if (mRecipeName.getText().toString().isEmpty()) {
            mRecipeName.setError("This field cannot be empty");
            mRecipeName.requestFocus();
            return;
        } else if (mRecipeType.getText().toString().isEmpty()) {
            mRecipeType.setError("This field cannot be empty");
            mRecipeType.requestFocus();
            return;
        } else if (mRecipePrepTime.getText().toString().isEmpty()) {
            mRecipePrepTime.setError("This field cannot be empty");
            mRecipePrepTime.requestFocus();
            return;
        } else if (mRecipePrepTime.getText().toString().length() > 3) {
            mRecipePrepTime.setError("The entered value is too large");
            mRecipePrepTime.requestFocus();
            return;
        } else if (mRecipeCookingTime.getText().toString().isEmpty()) {
            mRecipeCookingTime.setError("This field cannot be empty");
            mRecipeCookingTime.requestFocus();
            return;
        } else if (mRecipeCookingTime.getText().toString().length() > 3) {
            mRecipeCookingTime.setError("The entered value is too large");
            mRecipeCookingTime.requestFocus();
            return;
        } else if (mRecipeIngredients.getText().toString().isEmpty()) {
            mRecipeIngredients.setError("This field cannot be empty");
            mRecipeIngredients.requestFocus();
            return;
        } else if (mRecipePrepSteps.getText().toString().isEmpty()) {
            mRecipePrepSteps.setError("This field cannot be empty");
            mRecipePrepSteps.requestFocus();
            return;
        }

        final UserRecipe userRecipe = new UserRecipe();
        if(mRecipe != null) {
            userRecipe.setId(mRecipe.getId());
        }
        userRecipe.setName(mRecipeName.getText().toString());
        userRecipe.setType(mRecipeType.getText().toString());
        userRecipe.setPreparationTime(Integer.parseInt(mRecipePrepTime.getText().toString()));
        userRecipe.setCookingTime(Integer.parseInt(mRecipeCookingTime.getText().toString()));
        userRecipe.setIngredients(mRecipeIngredients.getText().toString());
        userRecipe.setPreparationSteps(mRecipePrepSteps.getText().toString());
        userRecipe.setPictureLocation(mRecipePicturePath);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                Recipe.addOrModifyUserRecipe(userRecipe);
                return null;
            }
            @Override
            protected void onPostExecute(String a) {
                Toast.makeText(getApplicationContext(), "Succsexfully saved!", Toast.LENGTH_LONG).show();
                finish();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteRecipe() {
        if (mRecipe != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new AsyncTask<Void, Void, Void>() {
                                @SuppressWarnings("ResultOfMethodCallIgnored")
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    if (mRecipePicturePath != null) {
                                        File f = new File(mRecipePicturePath.substring(7));
                                        if (f.exists()) {
                                            f.delete();
                                        }
                                    }
                                    Recipe.initDataRepo(getApplication());
                                    UserRecipe userRecipe = new UserRecipe();
                                    userRecipe.setId(mRecipe.getId());
                                    Recipe.deleteUserRecipe(userRecipe);
                                    return null;
                                }
                                @Override
                                protected void onPostExecute(Void v) {
                                    Toast.makeText(getApplicationContext(), "Succsexfully deleted!", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }.execute();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create()
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case IMAGE_PICKER_PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EasyImage.openChooserWithGallery(RecipeEditActivity.this, "Pick a picture for your recipe", 0);
                } else {
                    EasyImage.openGallery(RecipeEditActivity.this,  0);
                }
                break;
        }
    }
}
