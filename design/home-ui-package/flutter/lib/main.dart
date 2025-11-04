import 'package:flutter/material.dart';

import 'screens/metric_detail.dart';
import 'strings.dart';
import 'widgets/bottom_nav.dart';
import 'widgets/card_item.dart';
import 'widgets/tile_widget.dart';

void main() {
  runApp(const HealthApp());
}

/// Корневое приложение c Material 3 темой и гибридным макетом главного экрана.
class HealthApp extends StatelessWidget {
  const HealthApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: Strings.appTitle,
      debugShowCheckedModeBanner: false,
      themeMode: ThemeMode.system,
      theme: ThemeData(
        useMaterial3: true,
        colorSchemeSeed: const Color(0xFF6200EE),
        fontFamily: 'Roboto',
      ),
      darkTheme: ThemeData(
        useMaterial3: true,
        colorSchemeSeed: const Color(0xFF6200EE),
        brightness: Brightness.dark,
        fontFamily: 'Roboto',
      ),
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentIndex = 0;
  bool _isLoading = false;

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final tiles = _buildTileConfigs(colorScheme);
    final cards = _buildCardConfigs(colorScheme);

    return Scaffold(
      appBar: AppBar(title: const Text(Strings.appTitle)),
      bottomNavigationBar: HealthBottomNavigation(
        currentIndex: _currentIndex,
        onItemSelected: (index) {
          setState(() => _currentIndex = index);
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          setState(() => _isLoading = !_isLoading);
        },
        icon: Icon(_isLoading ? Icons.refresh : Icons.play_arrow_rounded),
        label: Text(_isLoading ? 'Остановить синхронизацию' : 'Запустить синхронизацию'),
      ),
      body: _currentIndex == 0
          ? SafeArea(
              bottom: false,
              child: CustomScrollView(
                slivers: [
                  SliverPadding(
                    padding: const EdgeInsets.all(16),
                    sliver: SliverGrid(
                      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 2,
                        mainAxisSpacing: 16,
                        crossAxisSpacing: 16,
                        childAspectRatio: 1,
                      ),
                      delegate: SliverChildBuilderDelegate(
                        (context, index) {
                          final tile = tiles[index];
                          return MetricTile(
                            title: tile.title,
                            subtitle: tile.subtitle,
                            value: tile.value,
                            color: tile.color,
                            isLoading: _isLoading,
                            onTap: () => _openDetails(tile.title, tile.value, tile.subtitle, tile.color),
                          );
                        },
                        childCount: tiles.length,
                      ),
                    ),
                  ),
                  SliverPadding(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    sliver: SliverList.separated(
                      itemBuilder: (context, index) {
                        final card = cards[index];
                        return MetricCard(
                          title: card.title,
                          subtitle: card.subtitle,
                          value: card.value,
                          icon: card.icon,
                          color: card.color,
                          isLoading: _isLoading,
                          onTap: () => _openDetails(card.title, card.value, card.subtitle, card.color),
                        );
                      },
                      separatorBuilder: (context, _) => const SizedBox(height: 4),
                      itemCount: cards.length,
                    ),
                  ),
                ],
              ),
            )
          : _buildPlaceholderTab(),
    );
  }

  Widget _buildPlaceholderTab() {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: const [
          Icon(Icons.construction, size: 64),
          SizedBox(height: 12),
          Text(Strings.loadingTitle),
          SizedBox(height: 4),
          Text(Strings.loadingSubtitle),
        ],
      ),
    );
  }

  void _openDetails(String title, String value, String subtitle, Color color) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => MetricDetailScreen(
          metricKey: title,
          value: value,
          subtitle: subtitle,
          color: color,
        ),
      ),
    );
  }

  List<_MetricConfig> _buildTileConfigs(ColorScheme scheme) {
    return [
      _MetricConfig(
        title: Strings.kpiPulseTitle,
        subtitle: Strings.kpiPulseSubtitle,
        value: '78 уд/мин',
        color: const Color(0xFFEF4444),
      ),
      _MetricConfig(
        title: Strings.kpiStepsTitle,
        subtitle: Strings.kpiStepsSubtitle,
        value: '3 242',
        color: const Color(0xFF10B981),
      ),
      _MetricConfig(
        title: Strings.kpiCaloriesTitle,
        subtitle: Strings.kpiCaloriesSubtitle,
        value: '1 245 ккал',
        color: const Color(0xFFFB923C),
      ),
      _MetricConfig(
        title: Strings.kpiSleepTitle,
        subtitle: Strings.kpiSleepSubtitle,
        value: '7 ч 45 м',
        color: const Color(0xFF6366F1),
      ),
    ];
  }

  List<_MetricConfig> _buildCardConfigs(ColorScheme scheme) {
    return [
      _MetricConfig(
        title: Strings.cardBloodPressureTitle,
        subtitle: Strings.cardBloodPressureSubtitle,
        value: '118/76',
        color: const Color(0xFF2563EB),
        icon: Icons.monitor_heart,
      ),
      _MetricConfig(
        title: Strings.cardWeightTitle,
        subtitle: Strings.cardWeightSubtitle,
        value: '68,2 кг',
        color: const Color(0xFF9333EA),
        icon: Icons.monitor_weight,
      ),
      _MetricConfig(
        title: Strings.cardOxygenTitle,
        subtitle: Strings.cardOxygenSubtitle,
        value: '97%',
        color: const Color(0xFF0EA5E9),
        icon: Icons.bubble_chart_outlined,
      ),
      _MetricConfig(
        title: Strings.cardRespiratoryTitle,
        subtitle: Strings.cardRespiratorySubtitle,
        value: '14 в мин',
        color: const Color(0xFF14B8A6),
        icon: Icons.air_rounded,
      ),
    ];
  }
}

class _MetricConfig {
  const _MetricConfig({
    required this.title,
    required this.subtitle,
    required this.value,
    required this.color,
    this.icon,
  });

  final String title;
  final String subtitle;
  final String value;
  final Color color;
  final IconData? icon;
}
