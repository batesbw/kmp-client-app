# Project Plan: kmp-client-app UI/UX and Feature Parity with Frontend

## 1. Overall Goal

The primary goal of this project is to significantly enhance the `kmp-client-app` (Android application) to closely mirror the User Interface (UI), User Experience (UX), and core functionality of the existing web-based `frontend`. This involves understanding the `frontend`'s design, its interaction patterns with the `server`, and replicating these aspects within the Android app using Kotlin Multiplatform (KMP) and Jetpack Compose. **The initial development will prioritize establishing robust core media player functionality, including playback, queue management, and basic media selection.**

## 2. Project Scope

*   **UI/UX Overhaul:** Redesign and implement Android app screens and components to match the `frontend`'s aesthetics and usability.
*   **Feature Parity:** Implement key features currently available in the `frontend` that are missing or different in the Android app.
*   **Prioritized Player Implementation:** Focus initial development cycles on delivering a functional media player capable of basic library interaction, playback control, and queue management.
*   **Code Structure and Modularity:** Organize the Android app's codebase into clearly separated modules, grouped by feature or responsibility, similar to best practices potentially observed in the `frontend`.
*   **API Interaction:** Ensure the Android app interacts with the `server` in a manner consistent with how the `frontend` does, reusing or adapting data models and API call logic where appropriate.

## 3. Key Areas of Focus (Refined based on `frontend` and `server` exploration)

*   **Core Music Playback & Control (Highest Priority):**
    *   "Now Playing" screen (potentially full-screen and a mini/footer version).
    *   Core playback controls: play, pause, skip (next/previous), seek, volume.
    *   Player queue management (viewing, adding, removing, reordering tracks) - maps to `server/music_assistant/controllers/player_queues.py`.
    *   Shuffle and repeat functionalities.
    *   Interaction with `server/music_assistant/controllers/players.py` for player state and commands.
    *   Basic media selection from library to initiate playback.
    *   Lyrics display (`LyricsViewer.vue` equivalent) - *Secondary to core playback.*
    *   Audio quality selection (`QualityDetailsBtn.vue` equivalent) - *Secondary to core playback.*
    *   Player selection if multiple players are supported (`PlayerSelect.vue`) - *Core if multiple players are essential early on, otherwise secondary.*
*   **Core Navigation & Layout (Supporting Player Functionality):**
    *   Establish a main layout structure to host screens and persistent elements like a mini-player/footer.
    *   Minimal `BottomNavigation` for access to essential player-related sections (e.g., Now Playing, simplified Library/Queue).
    *   Screen routing for player and basic selection screens.
    *   Implement a `DrawerNavigation` for secondary items, similar to `frontend` - *Secondary to initial player focus.*
*   **Music Library Browsing (Initial focus on playback selection, full parity is secondary):**
    *   Simplified views for: Tracks, and potentially Albums or Playlists, primarily to allow users to select something to play.
    *   Listings of items with thumbnails and essential info (`ItemsListing.vue` equivalents, simplified initially).
    *   Full replication of `frontend/src/views/Library*.vue` and detailed views (`AlbumDetails.vue`, etc.) is a *secondary priority* after core player is solid.
    *   Context Menus for items (`ItemContextMenu.vue` equivalent) for actions like play, add to queue - *Essential actions first, full menu secondary.*
    *   Favorite button functionality (`FavoriteButton.vue`) - *Secondary.*
*   **Home/Discovery (Lower Priority):**
    *   `HomeView.vue` equivalent, possibly using `HomeWidgetRows.vue` and `Carousel.vue` concepts for a dynamic home screen.
    *   `BrowseView.vue` equivalent for general content discovery.
*   **Search (Lower Priority):**
    *   Implement a comprehensive search feature (`Search.vue` equivalent) to find tracks, albums, artists, etc.
*   **Settings (Essential player settings first, full parity lower priority):**
    *   Minimal settings related to player behavior or server connection if necessary for MVP.
    *   Full replication of `frontend/src/views/settings/` is a *lower priority*.
*   **Theming and Styling (Basic setup first):**
    *   Implement basic dynamic theming (light/dark modes) consistent with `frontend/src/styles/`.
    *   Consistent typography, iconography (`SvgIcon.vue`), and color palette.
*   **State Management:**
    *   Adopting a robust state management solution (e.g., ViewModel with StateFlow/SharedFlow) suitable for Compose and KMP, especially for player state.
*   **API Interaction Layer:**
    *   Develop a clear API service layer in the KMP app to interact with the `server` endpoints identified (primarily in `server/music_assistant/controllers/` and its `media/` subdirectory), focusing on player and basic library endpoints first.
    *   Define KMP data models corresponding to `server` responses.

## 4. Architectural Approach

*   **UI Toolkit:** Jetpack Compose will be the primary UI toolkit for the Android app, leveraging its declarative nature to build modern and responsive UIs.
*   **Kotlin Multiplatform (KMP):** Maximize code sharing (business logic, data layers, view models) between platforms if applicable and as the project evolves. For now, the focus is Android.
*   **MVVM (Model-View-ViewModel) or similar:** Employ a suitable architectural pattern to separate concerns and improve testability and maintainability.
*   **Dependency Injection:** Utilize a DI framework (e.g., Koin, Hilt) for managing dependencies.
*   **Networking:** Use a modern networking library (e.g., Ktor, Retrofit) for API communication with the `server`.
*   **Data Persistence:** Implement local caching or data persistence strategies if the `frontend` utilizes them (e.g., for `server/music_assistant/controllers/cache.py` interactions) for improved performance and offline capabilities.

## 5. Project Phases (High-Level - Adjusted for Player Priority)

1.  **Phase 1: Analysis and Planning (Current Phase)**
    *   Detailed analysis of `frontend` UI, UX, and features.
    *   Analysis of `server` API endpoints and data models used by `frontend`, with a focus on player and media item interactions.
    *   Refinement of this `PLANNING.md` document.
    *   Creation of detailed tasks in `TASK.md` reflecting player-first priority.
2.  **Phase 2: Core Player Implementation & Basic UI**
    *   Set up basic project structure, theming (light/dark), and essential navigation framework (e.g., simple bottom navigation for player, queue, minimal library).
    *   Implement core player logic: playback state management, playback controls (play/pause, skip, seek, volume via API calls), audio focus handling.
    *   Develop UI for a functional "Now Playing" screen and a persistent mini-player.
    *   Implement core queue management (viewing current queue, basic add/remove actions via API calls).
    *   Implement basic media browsing/selection screens (e.g., list of tracks/albums) sufficient to choose media and initiate playback.
3.  **Phase 3: Library, UI Parity, and Feature Expansion (Iterative)**
    *   Expand library browsing features (detailed views for artists, albums, playlists; advanced filtering and sorting in `ItemsListing` equivalents).
    *   Iteratively implement other UI sections and features from the `frontend` (e.g., full Search, comprehensive Settings, Home/Browse views).
    *   Refine UI/UX to more closely match `frontend` aesthetics and component behaviors (e.g., detailed context menus, advanced player features like lyrics, quality details).
    *   Implement `DrawerNavigation` and more complex navigation patterns.
4.  **Phase 4: Testing and Refinement**
    *   Thorough testing of implemented features.
    *   UI polishing and bug fixing.
    *   Performance optimization.
5.  **Phase 5: Documentation and Release Preparation**
    *   Update `README.md` and any other relevant documentation.
    *   Prepare for release.

## 6. Key Success Metrics

*   High degree of visual similarity between the Android app and the `frontend`.
*   Availability of major `frontend` features in the Android app.
*   Positive user feedback on the new UI/UX.
*   Maintainable and scalable codebase.

## 7. Assumptions

*   The `server` API is stable and well-documented (or can be understood by observing `frontend` interactions).
*   Access to the `frontend` and `server` codebases is sufficient for understanding their functionality.
*   The existing `kmp-client-app` can be refactored incrementally.

## 8. Risks and Mitigation

*   **Risk:** Underestimating the complexity of replicating certain `frontend` features, especially those tied to large server-side controller files like `music.py` (1918 lines), `player_queues.py` (2110 lines), `players.py` (1695 lines) and large frontend components like `ItemContextMenu.vue` (852 lines), `ItemsListing.vue` (1199 lines).
    *   **Mitigation:** Detailed upfront analysis and breaking down features into smaller, manageable tasks. Iterative development. Focus on core functionality first. For large components, plan to break them into smaller, maintainable Jetpack Compose functions/files, respecting the 500-line limit.
*   **Risk:** Discrepancies in platform capabilities (Android vs. Web).
    *   **Mitigation:** Adapt features thoughtfully for the Android platform, prioritizing core functionality and user experience.
*   **Risk:** Time constraints.
    *   **Mitigation:** Prioritize features and adopt an iterative approach. Clearly communicate progress and potential delays.

This document will be updated as the project progresses and more information is gathered. 