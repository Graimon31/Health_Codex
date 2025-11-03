import 'package:flutter/material.dart';

/// Карточка списка показателей с иконкой, подписью и значением.
class MetricCard extends StatelessWidget {
  const MetricCard({
    super.key,
    required this.title,
    required this.subtitle,
    required this.value,
    required this.icon,
    required this.color,
    this.onTap,
    this.isLoading = false,
  });

  final String title;
  final String subtitle;
  final String value;
  final IconData icon;
  final Color color;
  final VoidCallback? onTap;
  final bool isLoading;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      elevation: 2,
      margin: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          child: isLoading
              ? const _CardLoadingPlaceholder()
              : Row(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    CircleAvatar(
                      radius: 24,
                      backgroundColor: color.withOpacity(0.12),
                      child: Icon(icon, color: color),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            title,
                            style: theme.textTheme.titleMedium,
                          ),
                          const SizedBox(height: 4),
                          Text(
                            subtitle,
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Hero(
                      tag: 'metric-value-$title',
                      child: Text(
                        value,
                        style: theme.textTheme.headlineSmall?.copyWith(
                          color: color,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                  ],
                ),
        ),
      ),
    );
  }
}

class _CardLoadingPlaceholder extends StatefulWidget {
  const _CardLoadingPlaceholder();

  @override
  State<_CardLoadingPlaceholder> createState() => _CardLoadingPlaceholderState();
}

class _CardLoadingPlaceholderState extends State<_CardLoadingPlaceholder>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1300),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final baseColor = Theme.of(context).colorScheme.surfaceVariant;
    final highlight = Theme.of(context).colorScheme.onSurface.withOpacity(0.08);

    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return Row(
          children: [
            _circlePlaceholder(baseColor, highlight),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _rectPlaceholder(width: double.infinity, height: 16),
                  const SizedBox(height: 8),
                  _rectPlaceholder(width: 120, height: 14),
                ],
              ),
            ),
            const SizedBox(width: 16),
            _rectPlaceholder(width: 72, height: 24),
          ],
        );
      },
    );
  }

  Widget _circlePlaceholder(Color base, Color highlight) {
    return Container(
      height: 48,
      width: 48,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: LinearGradient(
          colors: [base, highlight, base],
          stops: const [0, 0.5, 1],
          transform: _SlidingGradientTransform(_controller.value),
        ),
      ),
    );
  }

  Widget _rectPlaceholder({required double width, required double height}) {
    final baseColor = Theme.of(context).colorScheme.surfaceVariant;
    final highlight = Theme.of(context).colorScheme.onSurface.withOpacity(0.08);
    return Container(
      width: width,
      height: height,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(12),
        gradient: LinearGradient(
          colors: [baseColor, highlight, baseColor],
          stops: const [0, 0.5, 1],
          transform: _SlidingGradientTransform(_controller.value),
        ),
      ),
    );
  }
}

class _SlidingGradientTransform extends GradientTransform {
  const _SlidingGradientTransform(this.progress);

  final double progress;

  @override
  Matrix4? transform(Rect bounds, {TextDirection? textDirection}) {
    final dx = (bounds.width * 2) * progress;
    return Matrix4.translationValues(dx - bounds.width, 0, 0);
  }
}
