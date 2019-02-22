package ma.tdg.supcooking.model;

import ma.tdg.supcooking.util.ServerApi;

public class User
{
    private String mUsername;
    private String mPassword;
    private String mPhoneNumber;
    private String mLastName;
    private String mFirstName;
    private String mAddress;
    private String mEmail;

    private static User sProfile;

    public static boolean doLogin(String username, String password) throws Exception {
        sProfile = ServerApi.doAuth(username, password);
        return sProfile != null;
    }

    public static void doLogout() {
        sProfile = null;
    }

    public static boolean isLoggedIn() {
        return sProfile != null;
    }

    public static User getProfile() {
        return sProfile;
    }

    public static void setProfile(User profile) {
        sProfile = profile;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getFullName() {
        return mFirstName + " " + mLastName;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }
}
