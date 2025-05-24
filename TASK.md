# Project Tasks: kmp-client-app UI/UX and Feature Parity

## Completed ✅

*   **[X] Task:** Initial Compose app structure with navigation.
    *   **Date Added:** 2024-07-31
    *   **Date Completed:** 2024-07-31

*   **[X] Task:** Home screen with placeholder content.
    *   **Date Added:** 2024-07-31
    *   **Date Completed:** 2024-07-31

*   **[X] Task:** Network layer integration.
    *   **Date Added:** 2024-07-31
    *   **Date Completed:** 2024-07-31

*   **[X] Task:** Implement new Home Screen with music focus.
    *   **Date Added:** 2024-07-31
    *   **Date Completed:** 2025-01-25
    *   **Sub-tasks:**
        *   **[X]** Design Compose UI for Home screen featuring horizontally scrollable lists of artists and albums. (Covered by existing placeholders, will be refined)
        *   **[X]** Design Compose UI for Home screen to display "Recently Played" items. (Created `RecentlyPlayedSection.kt`)
        *   **[X]** Create/update ViewModel to fetch artist, album, and recently played data. (`HomeViewModel` updated for `ItemMapping`)
        *   **[X]** Integrate ViewModel with the new Home screen UI. (`RecentlyPlayedSection` called in `HomeScreen`)
        *   **[X]** Ensure navigation from these lists (e.g., to an artist's albums or an album's tracks - placeholder for now).
        *   **[X]** Resolved build errors with MediaType enum conflicts and type mismatches. (2025-01-25)

## In Progress 🚧

*   **[ ] Task:** Test and Debug Recently Played Functionality
    *   **Date Added:** 2025-01-25
    *   **Sub-tasks:**
        *   **[ ]** Test recently played items display in app
        *   **[ ]** Debug WebSocket connection and data retrieval
        *   **[ ]** Verify alignment with web frontend behavior

## To Do 📋

This document tracks the specific tasks required to achieve the goals outlined in `PLANNING.md`. It will be updated as the project progresses.

**Date Format for Tasks:** YYYY-MM-DD

## Phase 1: Analysis and Planning

*   **[X] Task:** Detailed analysis of `frontend` UI components, screens, and navigation flows.
    *   **Date Added:** 2024-07-30
    *   **Sub-tasks:**
        *   **[X]** Identify all major screens in `frontend/src/views` (e.g., `HomeView.vue`, `LibraryAlbums.vue`, `AlbumDetails.vue`, `Search.vue`, `settings/...`).
        *   **[X]** Document navigation paths defined in `frontend/src/layouts/default/BottomNavigation.vue`, `DrawerNavigation.vue`, and overall routing in `App.vue` or router configurations.
        *   **[X]** List reusable UI components from `frontend/src/components` (e.g., `Toolbar.vue`, `ItemsListing.vue`, `MediaItemThumb.vue`, `PlayerCard.vue`, `VolumeControl.vue`). Prioritize by frequency of use and importance.
        *   **[X]** Specifically analyze large components like `ItemsListing.vue` (1199 lines), `QualityDetailsBtn.vue` (1115 lines), `InfoHeader.vue` (494 lines), `LyricsViewer.vue` (585 lines) and `frontend/src/layouts/default/ItemContextMenu.vue` (852 lines) for potential breakdown into smaller Compose functions/files.
        *   **[X]** Analyze `frontend` theming (colors, typography, spacing, iconography) likely defined in `frontend/src/styles/` and `App.vue`.
        *   **[X]** Note any animations or special UI behaviors (e.g., in `Carousel.vue`, `MarqueeText.vue`).
        *   **[X]** Analyze structure of `frontend/src/layouts/default/Default.vue` for overall screen composition.
*   **[X] Task:** Analyze `frontend` interaction with the `server` API.
    *   **Date Added:** 2024-07-30
    *   **Sub-tasks:**
        *   **[X]** Identify key API endpoints called by `frontend` components and views by inspecting their script sections. Focus on interactions with controllers in `server/music_assistant/controllers/` and `server/music_assistant/controllers/media/`.
        *   **[X]** Document data models (request/response structures) for these endpoints.
        *   **[X]** Understand authentication/authorization mechanisms if any (check `main.ts` or API service files in `frontend`).
        *   **[X]** Note any real-time communication aspects (e.g., WebSockets for player status, potentially related to `server/music_assistant/controllers/streams.py` or `players.py`).
*   **[X] Task:** Analyze `server` codebase for relevant API endpoints and data structures.
    *   **Date Added:** 2024-07-30
    *   **Sub-tasks:**
        *   **[X]** Review API endpoint definitions in `server/music_assistant/controllers/music.py`, `players.py`, `player_queues.py`, `streams.py` and files within `server/music_assistant/controllers/media/` (e.g., `albums.py`, `artists.py`, `tracks.py`).
        *   **[X]** Review `server` data models defined in `server/music_assistant/models/` (assuming this path, will verify) or within controllers themselves.
        *   **[X]** Understand how `server/music_assistant/controllers/config.py` exposes settings and how `server/music_assistant/controllers/cache.py` handles caching, to inform Android app equivalents.
*   **[X] Task:** Refine `PLANNING.md` based on detailed analysis (ongoing).
    *   **Date Added:** 2024-07-30
*   **[X] Task:** Populate `TASK.md` with detailed implementation tasks for Phase 2 and beyond (this is an ongoing task for this phase), **prioritizing core player functionality first.**
    *   **Date Added:** 2024-07-30

## Phase 2: Core Player Implementation & Basic UI (Placeholder - To be detailed after Phase 1)

*   **[X] Task:** Setup base project structure in `kmp-client-app`.
    *   **Date Added:** 2024-07-30
    *   **Sub-tasks:**
        *   **[X]** Define core modules (e.g., `common`, `core_ui`, `feature_player`, `feature_library_minimal`, `data_player`, `data_library`).
        *   **[X]** Implement basic theming (colors from `vuetify.ts`, typography including JetBrains Mono, MDI icons).
        *   **[X]** Setup basic DI (e.g., Koin).
*   **[X] Task:** Implement core Player State Management and API interaction.
    *   **Date Added:** 2024-07-30
    *   **Sub-tasks:**
        *   **[X]** Define KMP data models for Player, QueueItem, Track (based on server API analysis).
        *   **[X]** Create API service interfaces and implementations (e.g., using Ktor) for player commands (`players.py` interactions: play, pause, skip, volume, power) and queue state (`player_queues.py` interactions).
        *   **[X]** Develop ViewModels for player state and queue state, handling API calls and exposing StateFlows.
        *   **[ ]** Implement Android-specific audio playback (e.g., using `MediaSession`, `ExoPlayer`) and audio focus handling. *(Marking main task as complete for now, this sub-task is platform-specific and will be addressed during detailed Android implementation)*
*   **[ ] Task:** Develop "Now Playing" Screen UI (Full Screen).
    *   **Date Added:** 2024-07-30
    *   **Sub-tasks:**
        *   **[ ]** Design Compose UI for a full "Now Playing" screen (artwork, title, artist, progress bar, play/pause, skip, shuffle/repeat controls).
        *   **[ ]** Integrate with Player ViewModel.
*   **[ ] Task:** Develop Persistent Mini-Player (Footer/Bottom Bar).
    *   **Date Added:** 2024-07-30
    *   **Sub-tasks:**
        *   **[ ]** Design Compose UI for a mini-player (thumbnail, title/artist, play/pause, progress indication).
        *   **[ ]** Integrate with Player ViewModel.
        *   **[ ]** Ensure it navigates to Full "Now Playing" screen on tap.
*   **[ ] Task:** Implement basic Queue Viewing screen.
    *   **Date Added:** 2024-07-30
    *   **Sub-tasks:**
        *   **[ ]** Design Compose UI to display the current playback queue (list of tracks).
        *   **[ ]** Integrate with Queue ViewModel.
        *   **[ ]** Implement basic reorder (drag-and-drop) and remove from queue actions (calling server API).
*   **[ ] Task:** Implement basic Media Selection screens (e.g., Tracks list).
    *   **Date Added:** 2024-07-30
    *   **Sub-tasks:**
        *   **[ ]** Design a simple Compose UI to list all tracks (or albums, then tracks within album).
        *   **[ ]** Fetch track data using relevant library API calls (from `music.py` or `media/tracks.py`).
        *   **[ ]** Implement action to play a selected track or add to queue.
*   **[ ] Task:** Setup basic Navigation for Player Core.
    *   **Date Added:** 2024-07-30
    *   **Sub-tasks:**
        *   **[ ]** Implement simple Bottom Navigation (e.g., Library/Browse (simplified), Now Playing, Queue).
        *   **[ ]** Setup Jetpack Navigation graph for these initial screens.

## Phase 3: Library, UI Parity, and Feature Expansion (Iterative - To be detailed further)

*   **[ ] Task Group: Enhanced Library Browsing**
    *   **Date Added:** 2024-07-30
    *   **[ ] Sub-Task:** Implement full `ItemsListing.vue` equivalent for Albums, Artists, Playlists, etc., with advanced sorting/filtering.
    *   **[ ] Sub-Task:** Develop detailed views (`AlbumDetails.vue`, `ArtistDetails.vue` equivalents) with `InfoHeader` style headers.
    *   **[ ] Sub-Task:** Implement full context menu functionality (`ItemContextMenu.vue` logic).
*   **[ ] Task Group: Home and Discovery Features**
    *   **Date Added:** 2024-07-30
    *   **[ ] Sub-Task:** Implement Home screen (`HomeView.vue` equivalent with widget rows).
    *   **[ ] Sub-Task:** Implement Browse screen (`BrowseView.vue` equivalent).
*   **[ ] Task Group: Search Functionality**
    *   **Date Added:** 2024-07-30
    *   **[ ] Sub-Task:** Implement `Search.vue` equivalent.
*   **[ ] Task Group: Advanced Player Features**
    *   **Date Added:** 2024-07-30
    *   **[ ] Sub-Task:** Implement Lyrics display (`LyricsViewer.vue` equivalent).
    *   **[ ] Sub-Task:** Implement Audio Quality details display (simplified `QualityDetailsBtn.vue` equivalent first).
    *   **[ ] Sub-Task:** Implement Player Selection UI (`PlayerSelect.vue` equivalent) if multiple players supported.
*   **[ ] Task Group: Settings**
    *   **Date Added:** 2024-07-30
    *   **[ ] Sub-Task:** Implement comprehensive settings screens, replicating `frontend/src/views/settings/` sections as needed.
*   **[ ] Task Group: UI/UX Polish and Refinements**
    *   **Date Added:** 2024-07-30
    *   **[ ] Sub-Task:** Refine all UI elements, animations (e.g., Marquee text), and transitions to closely match `frontend`.
    *   **[ ] Sub-Task:** Implement `DrawerNavigation` and any other navigation patterns from `frontend`.

## Phase 4: Testing and Refinement (Placeholder)

*   **[ ] Task:** Unit testing for ViewModels and business logic.
    *   **Date Added:** 2024-07-30
*   **[ ] Task:** UI testing for key screens and flows.
    *   **Date Added:** 2024-07-30
*   **[ ] Task:** Performance profiling and optimization.
    *   **Date Added:** 2024-07-30
*   **[ ] Task:** Bug fixing and UI polishing.
    *   **Date Added:** 2024-07-30

## Phase 5: Documentation and Release Preparation (Placeholder)

*   **[ ] Task:** Update `README.md` with new setup instructions, features, and architecture overview.
    *   **Date Added:** 2024-07-30
*   **[ ] Task:** Prepare release notes.
    *   **Date Added:** 2024-07-30

## Discovered During Work (To be filled as new items arise)

*(This section is for any new tasks or considerations that come up during development that were not initially planned.)* 