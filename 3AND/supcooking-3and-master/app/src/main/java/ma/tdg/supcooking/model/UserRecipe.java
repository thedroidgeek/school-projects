package ma.tdg.supcooking.model;

import android.arch.persistence.room.Entity;

// this is a dummy class to force room to make two separate tables for the same java class
@Entity(tableName = "user_recipe", inheritSuperIndices = true)
public class UserRecipe extends Recipe { }
