# Traffic Sniffer Settings Explained

This document explains what each setting is intended to do, what default it uses, and whether it is currently wired into runtime behavior.

## How settings are stored

- All settings are stored in `SharedPreferences` (`sniffer_settings`) via `SettingsState`.
- The Settings UI updates both in-memory state and persistent storage immediately.
- `Reset all settings` clears preferences and restores all defaults in memory.

## Important implementation note

Some settings are already connected to active capture behavior, while others are currently UI + persistence only (prepared for future wiring). This is normal for a project in active development.

---

## 1) Capture Settings

### Start capture on launch (`capture_on_start`, default: `false`)
- Intended: automatically begin packet capture when app opens.
- Current state: saved and shown in UI, but startup flow does not auto-start yet.

### Max log lines (`max_log_lines`, default: `500`)
- Intended: maximum number of packet summary lines retained in UI memory.
- Current state: the runtime log truncation is currently hardcoded to 500 lines in `MainActivity`; setting value is not yet used there.

### Snap length (`snap_length`, default: `65535`)
- Intended: maximum bytes captured per packet.
- Current state: saved in settings, but pcap writer and packet handling currently use full raw packet bytes; no truncation by this setting yet.

### Promiscuous mode (`promiscuous_mode`, default: `false`)
- Intended: capture traffic beyond device-targeted flows.
- Current state: persisted only. Android `VpnService` TUN capture behavior is currently unaffected by this toggle.

### Auto-stop after MB (`auto_stop_mb`, default: `0`)
- Intended: stop capture when written data reaches threshold.
- `0` means unlimited.
- Current state: persisted only; no active size-based stop logic yet.

### Auto-stop after minutes (`auto_stop_minutes`, default: `0`)
- Intended: stop capture after a time limit.
- `0` means unlimited.
- Current state: persisted only; no active timer-based stop logic yet.

---

## 2) Protocol Filters

### Capture TCP (`capture_tcp`, default: `true`)
- Intended: include/exclude TCP packets.
- Current state: persisted only; packet stream is not yet filtered by this flag.

### Capture UDP (`capture_udp`, default: `true`)
- Intended: include/exclude UDP packets.
- Current state: persisted only; not yet applied at capture/parser stage.

### Capture ICMP (`capture_icmp`, default: `true`)
- Intended: include/exclude ICMP packets.
- Current state: persisted only.

### Capture DNS (`capture_dns`, default: `true`)
- Intended: include/exclude DNS traffic.
- Current state: persisted only; no DNS-specific filter path yet.

### Capture HTTP (`capture_http`, default: `true`)
- Intended: include/exclude plain HTTP traffic.
- Current state: persisted only.

### Capture HTTPS (`capture_https`, default: `true`)
- Intended: include/exclude encrypted HTTPS/TLS traffic.
- Current state: persisted only.

---

## 3) VPN Settings

### VPN Address (`vpn_address`, default: `10.0.0.2`)
- Intended: local TUN interface address.
- Current state: currently hardcoded in `LocalVpnService` builder (`10.0.0.2`). Setting exists but is not yet read from `SettingsState`.

### DNS Server (`vpn_dns`, default: `8.8.8.8`)
- Intended: DNS server configured in VPN tunnel.
- Current state: currently hardcoded in `LocalVpnService` (`8.8.8.8`), not yet dynamically bound to settings.

### MTU (`vpn_mtu`, default: `1500`)
- Intended: maximum VPN packet size.
- Current state: currently hardcoded to `1500` in `LocalVpnService`; setting is not yet wired.

### Allow apps to bypass VPN (`vpn_allow_bypass`, default: `false`)
- Intended: allow apps to request bypass when supported.
- Current state: persisted only; `VpnService.Builder` bypass APIs are not yet configured.

### Per-app VPN (`vpn_per_app`, default: `false`)
- Intended: capture only selected apps.
- Current state: persisted only; per-app include/exclude is not yet applied to VPN builder.

### Allowed Apps (`vpn_allowed_apps`, default: empty)
- Intended: app package list for per-app mode.
- Current state: selector entry is shown in UI when per-app mode is on, but applied package rules are not yet wired.

---

## 4) Proxy / MITM Settings

### Enable MITM Proxy (`proxy_enabled`, default: `false`)
- Intended: route traffic through local MITM bridge for deeper inspection.
- Current state: toggled and persisted in UI. `LocalVpnService` does not yet start/stop `MitmProxyBridge` based on this setting.

### Proxy Port (`proxy_port`, default: `8899`)
- Intended: listening port of local proxy.
- Current state: `MitmProxyBridge` currently uses internal constant `8899`; setting value is not yet injected.

### Decrypt TLS traffic (`mitm_decrypt_tls`, default: `false`)
- Intended: enable TLS interception after CA setup.
- Current state: persisted only; TLS interception handshake/certificate flow not implemented yet.

### MITM cert installed (`mitm_cert_installed`, default: `false`)
- Intended: state marker to indicate CA certificate installation.
- Current state: persisted flag only; certificate install and trust verification flow is not implemented yet.

---

## 5) Parser / Display Settings

### Resolve hostnames (`resolve_hostnames`, default: `false`)
- Intended: reverse-DNS names instead of raw IPs.
- Current state: persisted only; parser output currently shows IP addresses directly.

### Absolute timestamps (`show_absolute_ts`, default: `true`)
- Intended: display wall-clock timestamps instead of relative capture time.
- Current state: persisted only; packet list line format currently does not include timestamp mode switching.

### Hex dump payload (`hex_dump_payload`, default: `false`)
- Intended: display payload as hex in detail views.
- Current state: persisted only; payload hex rendering is not yet wired in packet detail pipeline.

### Color-code protocols (`color_protocols`, default: `true`)
- Intended: color packet rows by protocol type.
- Current state: UI already colors rows by prefix (TCP/UDP/ICMP), but this toggle is not yet used to enable/disable that behavior.

### Packet list font size (`font_size`, default: `12`)
- Intended: control packet list text size.
- Current state: persisted only; list text currently uses fixed typography.

---

## 6) Export / Storage Settings

### Export format (`export_format`, default: `pcap`)
- Intended: choose export format (`pcap`, `pcapng`, `csv`, `json`).
- Current state: saved in settings. Actual save flow currently exports active pcap file or raw text log; format conversion pipeline is not implemented.

### Auto-save captures (`auto_save`, default: `true`)
- Intended: automatically save capture on stop.
- Current state: persisted only; no auto-save trigger at capture stop yet.

### Max capture files (`max_capture_files`, default: `10`)
- Intended: retention policy limit for files in capture directory.
- Current state: persisted only; cleanup policy is not enforced yet.

### Compress exports (`compress_exports`, default: `false`)
- Intended: gzip output files.
- Current state: persisted only; no compression step in save/export flow yet.

---

## 7) Notification Settings

### Show notification (`show_notification`, default: `true`)
- Intended: control persistent foreground notification visibility.
- Current state: `LocalVpnService` always starts foreground notification; toggle is not yet applied.

### Show packet count (`show_packet_count`, default: `true`)
- Intended: include live packet count in notification text.
- Current state: persisted only; notification text is currently static.

### Vibrate on first capture (`vibrate_on_capture`, default: `false`)
- Intended: haptic feedback when first packet arrives.
- Current state: persisted only; no vibration code path yet.

---

## 8) Advanced Settings

### Buffer size (`buffer_size_kb`, default: `64`)
- Intended: tune internal read/processing buffers.
- Current state: persisted only; `TunReader` currently allocates by fixed MTU packet buffer.

### Enable IPv6 (`enable_ipv6`, default: `false`)
- Intended: enable IPv6 capture/parsing path.
- Current state: parser currently only handles IPv4 and returns `null` for non-IPv4; toggle is not yet wired.

### Log level (`log_level`, default: `Info`)
- Intended: configure internal logging verbosity.
- Current state: persisted only; logging calls do not yet reference this setting.

### Keep screen on (`keep_screen_on`, default: `false`)
- Intended: prevent display sleep during capture.
- Current state: persisted only; no active window flag management yet.

---

## 9) Danger Zone Actions

### Reset all settings
- Implemented.
- Clears `SharedPreferences` and restores defaults in `SettingsState`.

### Delete all captures
- Not implemented yet.
- UI dialog exists, but deletion code is currently marked TODO.

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
- `SharedPreferences`:
	- Persistent storage for all settings.

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
