# GroceryPing

GroceryPing is an Android application that helps users manage their grocery shopping with smart features like location-based store notifications, reminders, and price tracking.

## Features

### Grocery List Management
- Add, edit, and delete grocery items
- Mark items as complete
- Associate items with specific stores
- Track prices and quantities
- Categorize items

### Store Management
- Add and manage store locations
- Location-based store tracking
- Geofencing for store proximity alerts
- Customizable store radius
- Store activation toggle

### Smart Reminders
- Create one-time or repeating reminders
- Custom reminder messages
- Item-specific reminders
- Push notifications
- Flexible scheduling

### Location Services
- Location-based store notifications
- Geofence monitoring
- Background location tracking
- Battery optimization handling
- Permission management

## Technical Details

### Requirements
- Android Studio Arctic Fox or newer
- Android SDK 21+
- Google Play Services
- Location permissions
- Internet connection for geofencing

### Dependencies
- AndroidX
- Material Design Components
- Room Database
- LiveData
- ViewModel
- Location Services

## Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/GroceryPing.git
```

2. Open the project in Android Studio

3. Build and run the application

## Usage

1. **Adding Items**
   - Tap the + button to add new grocery items
   - Fill in item details (name, store, category, price, quantity)

2. **Managing Stores**
   - Access store management through the store FAB
   - Add store locations with addresses
   - Enable/disable store tracking

3. **Setting Reminders**
   - Create reminders for specific items
   - Set one-time or repeating reminders
   - Customize reminder messages

4. **Location Features**
   - Enable location services in the app
   - Receive notifications when near tracked stores
   - Manage store geofences

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Material Design Components
- Android Jetpack
- Google Play Services 