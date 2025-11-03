import 'package:flutter/material.dart';
import '../strings.dart';

/// Нижняя навигация c тремя вкладками согласно макету.
class HealthBottomNavigation extends StatelessWidget {
  const HealthBottomNavigation({
    super.key,
    required this.currentIndex,
    required this.onItemSelected,
  });

  final int currentIndex;
  final ValueChanged<int> onItemSelected;

  @override
  Widget build(BuildContext context) {
    return NavigationBar(
      selectedIndex: currentIndex,
      onDestinationSelected: onItemSelected,
      destinations: const [
        NavigationDestination(
          icon: Icon(Icons.dashboard_outlined),
          selectedIcon: Icon(Icons.dashboard_customize_rounded),
          label: Strings.navHome,
        ),
        NavigationDestination(
          icon: Icon(Icons.favorite_border),
          selectedIcon: Icon(Icons.favorite),
          label: Strings.navMeasurements,
        ),
        NavigationDestination(
          icon: Icon(Icons.person_outline),
          selectedIcon: Icon(Icons.person),
          label: Strings.navProfile,
        ),
      ],
    );
  }
}
