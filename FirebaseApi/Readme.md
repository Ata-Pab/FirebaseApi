# Firebase Api Tutorial
#### __*Start your Java Project with Maven in IntelliJ Idea.*__
#### __*Add `com.google.firebase` package into your `pom.xml` file (Version.6.12.2 in this project).*__
```
    <dependencies>
        <dependency>
            <groupId>com.google.firebase</groupId>
            <artifactId>firebase-admin</artifactId>
            <version>6.12.2</version>
        </dependency>
    </dependencies>
```
#### __*Add Firebase Project with your Google Account.*__
#### __*Set your database rules as (Test Mode Project Rules for 30 days):*__
```
rules_version = '2';
            service cloud.firestore {
              match /databases/{database}/documents {
                match /{document=**} {
                  allow read, write: if false;
                }
              }
            }
```
#### __*Go to Firebase `Project Settings` - `Service Accounts` - `Firebase Admin SDK` and `Generate New Private Key` (This is your Private Service Key in JSON Format)*__
#### __*Insert your Service Key JSON file into your Project Directory (Below the pom.xml).*__

#### __Firebase Api Initialization__
```
        Firebase firebase = new Firebase();
        firebase.app_CloudFirestoreInit();
```
#### __Set Collection name, Document name and Add Data to Cloud Firestore__
```
        String collectionName = "User_Informations";
        String documentName1 = "User1";

        Map<String, Object> allData = new HashMap<>();
        allData.put("First_Name", "Ata");
        allData.put("Last_Name", "Pub");
        allData.put("E_Mail", "pubMakes@gmail.com");
        allData.put("Age", 21);
        allData.put("Existing", true);

        firebase.app_AddDataToFirestore(collectionName, documentName1, allData);
```

#### __Update an Existing Document__
```
        firebase.app_UpdateData(collectionName, documentName1, "Password", "asd5456");
        // It does not have to be an existing Field name. If Field name exists, it updates old value with new value
```
#### __Add Second Document to the existing Collection__
```
        String documentName2 = "User2";

        Map<String, Object> allData2 = new HashMap<>();
        allData2.put("First_Name", "Ez");
        allData2.put("Last_Name", "Guv");
        allData2.put("E_Mail", "newUser@gmail.com");
        allData2.put("Age", 56);
        allData2.put("Existing", false);

        firebase.app_AddDataToFirestore(collectionName, documentName2, allData2);
```

#### __Delete Existing Field__
```
        firebase.app_DeleteField(collectionName, documentName2, "E_Mail");
```
#### __Delete Existing Document__
```
        firebase.app_DeleteDocument(collectionName, documentName2);
```

#### __Read an existing Data from an existing Document (As Map)__
```
        System.out.println(firebase.app_RetrieveDocument(collectionName, documentName2));
```

#### __Read an existing Data from an existing Field (As Integer, Long, Double, Boolean or String)__
```
        System.out.println(firebase.app_ReadDataFromDoc(collectionName, documentName2, "First_Name"));
        System.out.println(firebase.app_ReadDataFromDoc(collectionName, documentName2, "Age"));
        System.out.println(firebase.app_ReadDataFromDoc(collectionName, documentName2, "Existing"));
        // You can read Data in all data types (int, long, double, String, Boolean)
```

#### __Read All Existing Documents From Collection as Map__
```
        List<Map<String, Object>>  allDataMaps = firebase.app_RetrieveAllDocuments(collectionName);
        for(Map<String, Object> dataMapInDocument : allDataMaps)
        {
            System.out.println(dataMapInDocument);
        }
```

#### __Listen instant data changes in the Cloud Database (Set Listener)__
```
        app_SetRealTimeListener(collectionName, documentName2);
        /* This function prints all Data as Map in Document when any changes or updates happen.
           This function can be overridden or can be change its content (USER CODE HERE part) */
```

#### __Terminate Data Change Listener on Cloud Database__
```
        app_DetachRealTimeListener(collectionName);
```

#### __Learn Collection Existence with identified Document name__
```
        if(firebase.app_CollectionExistence(collectionName, documentName1) == Firebase.EXIST)
        {
            System.out.println("This Collection exists!");
        }
```