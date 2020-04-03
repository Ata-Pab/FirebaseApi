import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.protobuf.Api;

import javax.annotation.Nullable;
import javax.print.DocFlavor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Firebase {

    private FileInputStream serviceKeyFile;
    private FirebaseOptions fireOptions;
    private DocumentReference docRef;
    private ApiFuture<WriteResult> processResult;
    private ListenerRegistration listenerRegistration;
    private Firestore database;

    private String collection, document;
    private final String serviceKeyPath;
    private final String serviceKeyJson = "........YOUR_FIREBASE_PROJECT_SERVICE_KEY_JSON_FILE_HERE.json........";
    public static final Boolean EXIST = true;
    public static final Boolean NON_EXIST = false;

    public Firebase()
    {
        docRef = null;
        document = "DOCUMENT";
        collection = "COLLECTION";  // Default Values of Collection and Document
        serviceKeyPath = System.getProperty("user.dir") + "\\" + serviceKeyJson;
    }

    /* Real Time Database Initialization */
    public void app_FirebaseAdminInit()
    {
        try {
            /* This part is given in Firebase Service Accounts Page */
            serviceKeyFile  = new FileInputStream(serviceKeyPath);
            fireOptions = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceKeyFile))
                    .setDatabaseUrl("https://your_realtime_database_project_name.firebaseio.com")
                    .build();
            serviceKeyFile.close();
            FirebaseApp.initializeApp(fireOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Cloud Firestore Database Initialization */
    public void app_CloudFirestoreInit()
    {
        try {
            serviceKeyFile  = new FileInputStream(serviceKeyPath);
            fireOptions = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceKeyFile))
                    .build();
            FirebaseApp.initializeApp(fireOptions);
            database = FirestoreClient.getFirestore();
            serviceKeyFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* This Function always overwrites to Document -> If you want to some changes on your DB use app_UpdateData() */
    public void app_AddDataToFirestore(String collection, String document, Map<String, Object> allData)
    {
        this.collection = collection;
        this.document = document;

        // Set Collection and Document of Cloud Firestore Database
        docRef = database.collection(collection).document(document);

        processResult = docRef.set(allData);
        app_GetProcessResultTime(processResult);
    }

    public void app_UpdateData(String collection, String document, String field, Object data)
    {
        this.collection = collection;
        this.document = document;

        if(app_CollectionExistence(collection, document) == EXIST)
        {
            docRef = database.collection(collection).document(document);
            processResult = docRef.update(field, data);
            app_GetProcessResultTime(processResult);
        }
        else
            System.out.println("ERROR: No Existing Database Found Error!");
    }

    /*
     * When you delete a document, Cloud Firestore does not automatically delete the documents within its subcollections.
     * You can still access the subcollection documents by reference.
    */
    public void app_DeleteDocument(String collection, String document)
    {
        this.collection = collection;
        this.document = document;

        processResult = database.collection(collection).document(document).delete();
        app_GetProcessResultTime(processResult);
    }

    public void app_DeleteField(String collection, String document, String fieldName)
    {
        this.collection = collection;
        this.document = document;

        docRef = database.collection(collection).document(document);
        Map<String, Object> deleteMap = new HashMap<>();
        deleteMap.put(fieldName, FieldValue.delete());
        processResult = docRef.update(deleteMap);
        app_GetProcessResultTime(processResult);
    }

    public void app_DeleteCollection(String collectionName, int batchSize)
    {
        int deletedDoc = 0;
        try {
            CollectionReference collectionRef = database.collection(collectionName);
            ApiFuture<QuerySnapshot> future = collectionRef.limit(batchSize).get();
            List<QueryDocumentSnapshot> documentSnapshots = future.get().getDocuments();
            for(QueryDocumentSnapshot queryDocumentSnapshot : documentSnapshots)
            {
                queryDocumentSnapshot.getReference().delete();
                deletedDoc += 1;
            }
            if(deletedDoc >= batchSize)
            {
                app_DeleteCollection(collectionName, batchSize);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /* This function reads data of the existing Document and Returns All Data as Map */
    public Map<String, Object> app_RetrieveDocument(String collection, String document)
    {
        this.collection = collection;
        this.document = document;

        try {
            docRef = database.collection(collection).document(document);
            // Asynchronously Retrieve the Document
            ApiFuture<DocumentSnapshot> documentSnapshotFuture = docRef.get();
            DocumentSnapshot documentSnapshot = documentSnapshotFuture.get();
            if(documentSnapshot.exists())
                return documentSnapshot.getData();
            else
                return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("ERROR: An Error occurred when attempt to read data in document!");
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.out.println("ERROR: An Error occurred when attempt to read data in document!");
            return null;
        }
    }

    public List<Map<String, Object>> app_RetrieveAllDocuments(String collection)
    {
        this.collection = collection;
        this.document = "ALL_DOCUMENTS";
        List<Map<String, Object>> documentDataMaps = new ArrayList<>();

        try {
            // Asynchronously Retrieve all documents
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = database.collection(collection).get();
            List<QueryDocumentSnapshot> queryDocumentSnapshots = querySnapshotApiFuture.get().getDocuments();
            for(QueryDocumentSnapshot queryDocument : queryDocumentSnapshots)
            {
                documentDataMaps.add(queryDocument.getData());
            }
            return documentDataMaps;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* This function reads data of the existing Field and Returns Data as Object */
    public Object app_ReadDataFromDoc(String collection, String document, String fieldName)
    {
        this.collection = collection;
        this.document = document;

        try {
            docRef = database.collection(collection).document(document);
            // Asynchronously Retrieve the Document
            ApiFuture<DocumentSnapshot> documentSnapshotFuture = docRef.get();
            DocumentSnapshot documentSnapshot = documentSnapshotFuture.get();
            if(documentSnapshot.exists())
            {
                if(documentSnapshot.get(fieldName).getClass() == String.class)
                    return documentSnapshot.getString(fieldName);
                else if(documentSnapshot.get(fieldName).getClass() == Long.class)
                    return documentSnapshot.getLong(fieldName);
                else if(documentSnapshot.get(fieldName).getClass() == Boolean.class)
                    return documentSnapshot.getBoolean(fieldName);
                else if(documentSnapshot.get(fieldName).getClass() == Double.class)
                    return documentSnapshot.getDouble(fieldName);
                else
                    return null;
            }
            else
                return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("ERROR: An Error occurred when attempt to read data in document!");
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.out.println("ERROR: An Error occurred when attempt to read data in document!");
            return null;
        }
    }

    /* This function provides to Listen instant data changes on Cloud Database */
    public void app_SetRealTimeListener(String collection, String document)
    {
        this.collection = collection;
        this.document = document;

        docRef = database.collection(collection).document(document);
        listenerRegistration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirestoreException e) {
                if(e != null)
                    System.out.println("Cloud Firestore Database Listener Failed! " + e);
                else
                {
                    /* ========= USER CODE HERE ========= */
                    // If statement contents which are below can be changed with whatever user want
                    if(documentSnapshot != null && documentSnapshot.exists())
                        System.out.println("Database has been changed...\nInstant Document Data: " + documentSnapshot.getData());
                    else
                        System.out.println("No Change on Database...");
                    /* =================================== */
                }
            }
        });
    }

    /* This function provides to terminate Data Change Listener on Cloud Database */
    public void app_DetachRealTimeListener(String collection)
    {
        this.collection = collection;
        this.document = "DETACH_LISTENER";

        listenerRegistration.remove();
        System.out.println("Cloud Firestore Realtime Data Change Listener has been detached...");
    }

    public String app_GetInstantCollectionName()
    {
        return collection;
    }

    public String app_GetInstantDocumentName()
    {
        return document;
    }

    private void app_GetProcessResultTime(ApiFuture<WriteResult> processResult)
    {
        try {
            System.out.println("Data Update Time: " + processResult.get().getUpdateTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public Boolean app_CollectionExistence(String collection, String document)
    {
        docRef = database.collection(collection).document(document);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot documentSnapshot = future.get();
            if(documentSnapshot.exists())
                return EXIST;
            else
                return NON_EXIST;
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("ERROR: An Error occurred when attempt to Open Existing Collection!");
            return NON_EXIST;
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.out.println("ERROR: An Error occurred when attempt to Open Existing Collection!");
            return NON_EXIST;
        }
    }

    /* This function is still in the testing phase. It is not recommended to use! */
    public List<String> app_GetAllCollectionNames(String collection, String document)
    {
        List<String> collectionNames = new ArrayList<>();
        Iterable<CollectionReference> collectionReferences = database.collection(collection).document(document).listCollections();
        for(CollectionReference collectionReference : collectionReferences)
        {
            collectionNames.add(collectionReference.getId());
        }
        return collectionNames;
    }

    public String app_GetServiceKeyPath()
    {
        return serviceKeyPath;
    }

}


/*
============ Additional Informations ============
 Sometimes there isn't a meaningful ID for the document, and it's more convenient to let Cloud Firestore auto-generate an ID for you. You can do this by calling add():
Add document data with auto-generated id.
    Map<String, Object> data = new HashMap<>();
    data.put("name", "Tokyo");
    data.put("country", "Japan");
    ApiFuture<DocumentReference> addedDocRef = db.collection("cities").add(data);
    System.out.println("Added document with ID: " + addedDocRef.get().getId());
==================================================
* */
