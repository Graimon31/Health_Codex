import 'package:flutter/material.dart';
import '../strings.dart';

/// Экран детализации метрики с Hero-анимацией и заглушкой графика.
class MetricDetailScreen extends StatelessWidget {
  const MetricDetailScreen({
    super.key,
    required this.metricKey,
    required this.value,
    required this.subtitle,
    required this.color,
  });

  final String metricKey;
  final String value;
  final String subtitle;
  final Color color;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(metricKey)),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Hero(
              tag: 'metric-value-$metricKey',
              child: Text(
                value,
                style: theme.textTheme.displayMedium?.copyWith(
                  color: color,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            const SizedBox(height: 12),
            Text(
              subtitle,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              Strings.detailTrendTitle,
              style: theme.textTheme.titleLarge,
            ),
            const SizedBox(height: 12),
            Expanded(
              child: DecoratedBox(
                decoration: BoxDecoration(
                  color: theme.colorScheme.surfaceVariant,
                  borderRadius: BorderRadius.circular(24),
                ),
                child: Center(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Text(
                      Strings.detailDescription,
                      style: theme.textTheme.bodyMedium,
                      textAlign: TextAlign.center,
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: () {},
              icon: const Icon(Icons.info_outline),
              label: const Text(Strings.detailAction),
            ),
          ],
        ),
      ),
    );
  }
}
