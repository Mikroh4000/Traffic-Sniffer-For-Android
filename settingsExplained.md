# Traffic Sniffer Settings Explained

This document describes what each setting is intended to do, its default value, and whether it is currently connected to runtime behavior.

Last verified against code: March 2026.

## How settings are stored

 Settings are persisted with Jetpack DataStore Preferences (`sniffer_settings`) in `SettingsState`.
 A migration from legacy SharedPreferences (`sniffer_settings`) is configured.
 `Reset all settings` clears DataStore and restores in-memory defaults.

## Important implementation note

Some settings are fully wired into runtime behavior, while many are currently UI + persistence only (prepared for future wiring). This is expected for an in-progress project.

---

## 1) Capture Settings

### Start capture on launch (`capture_on_start`, default: `false`)
 Intended: automatically start packet capture when the app opens.
 Current state: implemented.
 Runtime behavior: when settings are loaded and this toggle is true, `MainActivity` triggers startup capture automatically.
 Note: Android VPN permission can still block immediate start until the user grants access.

### Max log lines (`max_log_lines`, default: `500`)
 Intended: maximum number of packet summary lines retained in memory.
 Current state: persisted and editable, but runtime truncation in `MainActivity` is still hardcoded to 500 lines.

### Snap length (`snap_length`, default: `65535`)
 Intended: maximum number of bytes captured per packet.
 Current state: persisted only. Capture/pcap writing still uses full packet bytes.

### Promiscuous mode (`promiscuous_mode`, default: `false`)
 Intended: capture traffic beyond device-targeted flows.
 Current state: persisted only. `VpnService` setup does not change based on this flag.

### Auto-stop after MB (`auto_stop_mb`, default: `0`)
 Intended: stop capture when written data reaches a threshold.
 `0` means unlimited.
 Current state: persisted only; no size-based stop logic is active.

### Auto-stop after minutes (`auto_stop_minutes`, default: `0`)
 Intended: stop capture after a time limit.
 `0` means unlimited.
 Current state: persisted only; no timer-based stop logic is active.

---

## 2) Protocol Filters

### Capture TCP (`capture_tcp`, default: `true`)
 Intended: include/exclude TCP packets.
 Current state: persisted only; not applied in packet processing.

### Capture UDP (`capture_udp`, default: `true`)
 Intended: include/exclude UDP packets.
 Current state: persisted only; not applied in packet processing.

### Capture ICMP (`capture_icmp`, default: `true`)
 Intended: include/exclude ICMP packets.
 Current state: persisted only; not applied in packet processing.

### Capture DNS (`capture_dns`, default: `true`)
 Intended: include/exclude DNS traffic.
 Current state: persisted only; no DNS-specific filtering path is active.

### Capture HTTP (`capture_http`, default: `true`)
 Intended: include/exclude HTTP traffic.
 Current state: persisted only.

### Capture HTTPS (`capture_https`, default: `true`)
 Intended: include/exclude HTTPS/TLS traffic.
 Current state: persisted only.

---

## 3) VPN Settings

### VPN Address (`vpn_address`, default: `10.0.0.2`)
 Intended: local TUN interface address.
 Current state: persisted in settings, but `LocalVpnService` still uses hardcoded `10.0.0.2`.

### DNS Server (`vpn_dns`, default: `8.8.8.8`)
 Intended: DNS server for the VPN tunnel.
 Current state: persisted in settings, but `LocalVpnService` still uses hardcoded `8.8.8.8`.

### MTU (`vpn_mtu`, default: `1500`)
 Intended: maximum VPN packet size.
 Current state: persisted in settings, but `LocalVpnService` still uses hardcoded `1500`.

### Allow apps to bypass VPN (`vpn_allow_bypass`, default: `false`)
 Intended: allow apps to bypass VPN when supported.
 Current state: persisted only; builder bypass API is not wired.

### Per-app VPN (`vpn_per_app`, default: `false`)
 Intended: capture only selected apps.
 Current state: persisted only; per-app include/exclude is not applied to VPN builder.

### Allowed Apps (`vpn_allowed_apps`, default: empty)
 Intended: package list used by per-app mode.
 Current state: UI entry exists when per-app mode is enabled, but selected apps are not applied to VPN routing.

---

## 4) Proxy / MITM Settings

### Enable MITM Proxy (`proxy_enabled`, default: `false`)
 Intended: route traffic through local MITM bridge for deeper inspection.
 Current state: persisted and visible in UI, but `LocalVpnService` does not start/stop `MitmProxyBridge` from this toggle.

### Proxy Port (`proxy_port`, default: `8899`)
 Intended: listening port of local proxy.
 Current state: persisted in settings, but `MitmProxyBridge` still uses internal constant `8899`.

### Decrypt TLS traffic (`mitm_decrypt_tls`, default: `false`)
 Intended: enable TLS interception after CA setup.
 Current state: persisted only; TLS interception flow is not implemented.

### MITM cert installed (`mitm_cert_installed`, default: `false`)
 Intended: marker indicating CA certificate installation.
 Current state: persisted flag only; certificate installation/verification workflow is not implemented.

---

## 5) Parser / Display Settings

### Resolve hostnames (`resolve_hostnames`, default: `false`)
 Intended: show hostnames instead of raw IP addresses.
 Current state: persisted only; parser output currently uses IP addresses directly.

### Absolute timestamps (`show_absolute_ts`, default: `true`)
 Intended: display wall-clock time instead of relative capture time.
 Current state: persisted only; packet list does not switch timestamp modes yet.

### Hex dump payload (`hex_dump_payload`, default: `false`)
 Intended: display payload as hexadecimal in packet detail views.
 Current state: persisted only; no hex payload rendering pipeline is active.

### Color-code protocols (`color_protocols`, default: `true`)
 Intended: enable/disable protocol-specific row colors.
 Current state: packet rows are always colorized by protocol prefix in `MainActivity`; this toggle is not yet used to control behavior.

### Packet list font size (`font_size`, default: `12`)
 Intended: control packet list text size.
 Current state: persisted only; list text still uses fixed typography values.

---

## 6) Export / Storage Settings

### Export format (`export_format`, default: `pcap`)
 Intended: choose export format (`pcap`, `pcapng`, `csv`, `json`).
 Current state: persisted in settings. Export flow currently writes active `.pcap` file if available, otherwise plain text log content.

### Auto-save captures (`auto_save`, default: `true`)
 Intended: auto-save capture when stopping.
 Current state: persisted only; no auto-save trigger on stop.

### Max capture files (`max_capture_files`, default: `10`)
 Intended: retention limit for stored capture files.
 Current state: persisted only; retention cleanup is not enforced.

### Compress exports (`compress_exports`, default: `false`)
 Intended: gzip-compress exported files.
 Current state: persisted only; no compression step in export flow.

---

## 7) Notification Settings

### Show notification (`show_notification`, default: `true`)
 Intended: control persistent foreground notification visibility.
 Current state: persisted only; `LocalVpnService` always starts as a foreground service with a notification.

### Show packet count (`show_packet_count`, default: `true`)
 Intended: include live packet count in notification text.
 Current state: persisted only; notification text is static.

### Vibrate on first capture (`vibrate_on_capture`, default: `false`)
 Intended: provide haptic feedback when first packet arrives.
 Current state: persisted only; no vibration path is implemented.

---

## 8) Advanced Settings

### Buffer size (`buffer_size_kb`, default: `64`)
 Intended: tune internal read/processing buffer sizes.
 Current state: persisted only; `TunReader` uses fixed MTU-sized buffer allocation.

### Enable IPv6 (`enable_ipv6`, default: `false`)
 Intended: enable IPv6 capture/parsing path.
 Current state: persisted only; parser currently accepts IPv4 packets only.

### Log level (`log_level`, default: `Info`)
 Intended: configure logging verbosity.
 Current state: persisted only; logging calls do not reference this setting.

### Keep screen on (`keep_screen_on`, default: `false`)
 Intended: prevent display sleep during capture.
 Current state: persisted only; no active window-flag handling.

---

## 9) Danger Zone Actions

### Reset all settings
 Implemented.
 Clears DataStore preferences and restores defaults in `SettingsState`.

### Delete all captures
 Not implemented yet.
 UI confirmation dialog exists, but delete action remains TODO in `SettingsScreen`.

---

## Tools and technologies used in this application

## Android platform tools/APIs

- `VpnService` + TUN interface:
	- Captures device traffic by establishing a local VPN tunnel.
	- Implemented in `LocalVpnService`.
- Foreground service + notifications:
	- Keeps capture service alive and visible while running.
- Storage Access Framework (SAF):
	- `ActivityResultContracts.CreateDocument` for export destination.
	- `ActivityResultContracts.OpenDocument` for loading files.
- Jetpack DataStore Preferences for settings persistence.

## Networking and packet handling tools

- `TunReader`:
	- Blocking read loop over TUN file descriptor for raw IP packets.
- `PacketParser`:
	- Parses IPv4 headers and TCP/UDP/ICMP metadata.
	- Builds one-line summaries similar to packet list tools.
- `PcapWriter`:
	- Writes standard `.pcap` files (global header + per-packet records), linktype RAW IP.
- `TcpForwarder`:
	- Coroutine-based TCP relay architecture for proxying/inspection.
- `MitmProxyBridge`:
	- Local TCP proxy server foundation for MITM workflows.
	- TLS decryption workflow is not fully implemented yet.

## UI and app architecture tools

- Jetpack Compose (`Material3`):
	- Declarative UI for home screen, packet list, and settings.
- Compose Navigation:
	- Route management for home/settings/detail screens.
- Compose state (`mutableStateOf`):
	- Live UI updates for packet logs and setting values.

## Concurrency and language/runtime tools

- Kotlin Coroutines:
	- Background packet loop and proxy tasks (`Dispatchers.IO`, scoped jobs).
- Java/Kotlin I/O + NIO:
	- File streams for pcap handling and socket channels for proxy forwarding.

## Build/test tooling

- Gradle Kotlin DSL (`build.gradle.kts`) + Version Catalog (`libs.versions.toml`).
- Android Gradle Plugin + Kotlin Compose plugin.
- Test stacks present:
	- JUnit 4 (unit tests)
	- AndroidX JUnit + Espresso (instrumented tests)
	- Compose UI test dependencies

---

## Practical takeaway

The project already has a strong technical foundation (VPN capture, parser, pcap writing, Compose UI). Many settings are fully persisted and surfaced in UI, while runtime wiring for advanced behavior (filtering, retention, TLS interception, dynamic VPN builder config) is the next major implementation step.
---
