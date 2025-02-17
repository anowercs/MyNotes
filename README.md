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
## Setup
1. Clone repository
2. Configure AWS:
   ```java
   // MyAmplifyApp.java
   public static final String S3_REGION = "YOUR_REGION";
   public static final String S3_BUCKET_NAME = "YOUR_BUCKET_NAME";

Dependencies
gradleCopydependencies {
    // AWS SDK
    implementation 'com.amazonaws:aws-android-sdk-core:2.x.x'
    implementation 'com.amazonaws:aws-android-sdk-dynamodb:2.x.x'
    implementation 'com.amazonaws:aws-android-sdk-cognito:2.x.x'
    
    // UI & Charts
    implementation 'com.github.bumptech.glide:glide:4.12.0'
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
}
Features in Development

 Offline support
 Note sharing
 Advanced fitness analytics
 Custom workout tracking
 Social features

Contributing
Feel free to submit issues and enhancement requests.
License
MIT License
