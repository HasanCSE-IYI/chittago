# Chittago (চিটাগো) 🗺️
### Open-Source, Offline-Ready Map & Navigation App for Chattogram Division

**Chittago** is a lightweight, zero-cost (100% free), production-grade map and navigation mobile application built for **Chattogram Division, Bangladesh**. 

Designed with modern Clean Architecture and offline-first capabilities, **Chittago** provides seamless exploration, turn-by-turn routing, POI discovery, and emergency services across all 11 districts of Chattogram division without requiring paid Google Maps APIs or credit cards.

---

## 🌟 Key Features
- **100% Free & Open-Source Stack:** Powered by OpenStreetMap (OSM), `flutter_map`, OSRM, and Komoot Photon API.
- **Chattogram Division Bounding:** Geofenced and optimized specifically for Chattogram, Cox's Bazar, Bandarban, Rangamati, Khagrachhari, Cumilla, Feni, Noakhali, Lakshmipur, Chandpur, and Brahmanbaria.
- **Offline Resilience:** Pre-cached points of interest (POI) and dynamic tile caching for network dead zones in the Hill Tracts.
- **Bilingual Search:** Natural forward and reverse geocoding supporting both Bengali (বাংলা) and English.
- **Turn-by-Turn Routing:** Free routing engine (OSRM) with realistic ETA, distance, and direction polylines for Car, Motorcycle, and Walking.
- **Emergency Hubs:** Quick access to hospitals, cyclone shelters, police stations, fire services, and coastal security.
- **Modern UI/UX:** Glassmorphic search bar, interactive bottom sheets, night/day map layers, and smooth 60 FPS transitions.

---

## 🏗️ Technology Stack
* **Framework:** Flutter (Dart 3)
* **Map Renderer:** `flutter_map` (Leaflet-inspired) + `latlong2`
* **Tiles:** CartoDB Positron / OSM Standard / OpenTopoMap
* **Geocoding & Autocomplete:** Komoot Photon API + OpenStreetMap Nominatim
* **Routing & Directions:** OSRM (Open Source Routing Machine)
* **Hardware Sensors:** `geolocator` (GPS) + `flutter_compass` (Heading/Direction)
* **Local Storage & Cache:** SQLite / SharedPreferences

---

## 🗺️ Covered Districts (চট্টগ্রাম বিভাগ)
1. Chattogram (চট্টগ্রাম)
2. Cox's Bazar (কক্সবাজার)
3. Bandarban (বান্দরবান)
4. Rangamati (রাঙ্গামাটি)
5. Khagrachhari (খাগড়াছড়ি)
6. Cumilla (কুমিল্লা)
7. Feni (ফেনী)
8. Noakhali (নোয়াখালী)
9. Lakshmipur (লক্ষ্মীপুর)
10. Chandpur (চাঁদপুর)
11. Brahmanbaria (ব্রাহ্মণবাড়িয়া)

---

## 👨‍💻 Author
**Hasan** ([@HasanCSE-IYI](https://github.com/HasanCSE-IYI))
