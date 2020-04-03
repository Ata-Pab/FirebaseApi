import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;

import java.util.ArrayList;
import java.util.List;

public class FirebaseAuth {

    private UserRecord userRecord;
    private UserRecord.CreateRequest createRequest;
    private String userName, userPsswrd, userEmail;

    public FirebaseAuth()
    {
        userName = null;
        userPsswrd = null;
        userEmail = null;
    }

    public void app_AddUserToCloud(String userName, String userEmail, String userPsswrd)
    {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPsswrd = userPsswrd;

        try {
            if(app_CheckUserEmailExistence(userEmail) == Firebase.NON_EXIST)
            {
                createRequest = new UserRecord.CreateRequest().setDisplayName(userName).setEmail(userEmail).setPassword(userPsswrd);
                userRecord = com.google.firebase.auth.FirebaseAuth.getInstance().createUser(createRequest);
                System.out.println("New User has been added to Database successfully!");
            }
            else
            {
                System.out.println("This user Email Address already exists");
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            System.out.println("ERROR: An Error occurred when attempt to Create New user!");
        }

        /* By default, Firebase Authentication will generate a random uid for the new user.
         * If you instead want to specify your own uid for the new user, you can include it argument passed
         * to the user creation method:*/
    }

    public Boolean app_CheckUserEmailExistence(String userEmail)
    {
        try {
            userRecord = com.google.firebase.auth.FirebaseAuth.getInstance().getUserByEmail(userEmail);
            if(userRecord != null)
                return Firebase.EXIST;
            else
                return Firebase.NON_EXIST;
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return Firebase.NON_EXIST;
        }
    }

    public List<String> app_GetAllUserNames()
    {
        List<String> userNames = new ArrayList<>();
        try {
            ListUsersPage listUsersPage = com.google.firebase.auth.FirebaseAuth.getInstance().listUsers(null);
            while(listUsersPage != null)
            {
                for(ExportedUserRecord user : listUsersPage.getValues())
                {
                    userNames.add(user.getDisplayName());
                }
                listUsersPage = listUsersPage.getNextPage();
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            userNames = null;
        }
        return userNames;
    }

    public Boolean app_CheckPsswrdCompatibility(String userEmail, String userPsswrd)
    {
        return true;
    }


}

/*
============ Additional Informations ============
  Property Type Description
        uid	string	The uid to assign to the newly created user. If not provided, a random uid will be automatically generated.
        email	string	The user's primary email. Must be a valid email address.
        emailVerified	boolean	Whether or not the user's primary email is verified. If not provided, the default is false.
        phoneNumber	string	The user's primary phone number. Must be a valid E.164 spec compliant phone number.
        password	string	The user's raw, unhashed password. Must be at least six characters long.
        displayName	string	The users' display name.
        photoURL	string	The user's photo URL.
        disabled	boolean	Whether or not the user is disabled. true for disabled; false for enabled. If not provided, the default is false.

  Delete Users
  The Firebase Admin SDK allows deleting existing users by their uid:
  FirebaseAuth.getInstance().deleteUser(uid);
==================================================
* */



