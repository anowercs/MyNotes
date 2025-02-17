# Notes Pro with Fitness Tracking

## Description
Notes Pro combines personal note-taking with fitness tracking in one integrated Android app. Using AWS cloud services (DynamoDB, Cognito, S3) for seamless synchronization, users can manage their notes across devices while monitoring their daily step count. The app features secure authentication, image attachments, web links, and visual fitness progress tracking through interactive graphs.

## Features
### Notes Management
- Create, edit, and delete notes
- Image attachments and web links
- Color-coding system
- Real-time search
- Cross-device synchronization

### Fitness Tracking
- Daily step counter
- Interactive progress graphs
- Goal setting
- Activity history
- Visual progress tracking

## Technical Stack
### Cloud Services
- AWS DynamoDB for data storage
- AWS Cognito for authentication
- AWS S3 for image storage

### Frontend
- Android SDK
- Material Design components
- MPAndroidChart for graphs
- Glide for image loading

## Project Structure
```plaintext
app/
├── activities/
│   ├── MainActivity.java
│   ├── CreateNoteActivity.java
│   └── FitnessTrackingActivity.java
├── adapters/
│   └── NotesAdapter.java
├── database/
│   └── DynamoDBHelper.java
├── entities/
│   ├── Note.java
│   └── StepData.java
├── services/
│   └── StepCounterService.java
└── utility/
    └── UserUtils.java
```
## Setup
1. Clone repository
2. Configure AWS:
   ```java
   // MyAmplifyApp.java
   public static final String S3_REGION = "YOUR_REGION";
   public static final String S3_BUCKET_NAME = "YOUR_BUCKET_NAME";

## Dependencies
```gradle
dependencies {
    // Amplify core dependency
    implementation libs.aws.android.sdk.core
    implementation libs.core
    implementation libs.aws.auth.cognito

    //AWS Authentication
    implementation libs.aws.android.sdk.auth.core
    implementation libs.aws.android.sdk.auth.userpools
    implementation libs.aws.android.sdk.cognitoidentityprovider


    //AWS Services
    //implementation libs.aws.api // Optional: for API integration
    implementation libs.aws.android.sdk.pinpoint
    implementation libs.aws.android.sdk.s3
    implementation libs.aws.storage.s3
    //implementation libs.aws.android.sdk.dynamodb
    implementation libs.aws.datastore
    implementation libs.aws.android.sdk.mobile.client
    implementation libs.aws.android.sdk.ddb.mapper


    //Image loader
    implementation libs.glide
    annotationProcessor libs.compiler


    //UI Components
    implementation libs.sdp.android
    implementation libs.ssp.android
    implementation libs.roundedimageview


    //Room Database
    //implementation libs.room.runtime
    //annotationProcessor libs.room.compiler

    // Support for Java 8 features
    coreLibraryDesugaring libs.desugar.jdk.libs


    //implementation libs.dynamodb.v220145

    implementation libs.lottie

    //walk and graph
    implementation libs.play.services.fitness
    implementation libs.mpandroidchart // For the graph

}
```

## Features in Development
 Offline support
 Note sharing
 Advanced fitness analytics
 Custom workout tracking
 Social features

## Contributing
Feel free to submit issues and enhancement requests.
## License
MIT License
