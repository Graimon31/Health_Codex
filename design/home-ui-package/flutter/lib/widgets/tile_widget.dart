import 'dart:math' as math;

import 'package:flutter/material.dart';

/// Большая KPI-плитка с заголовками, значением и мини-графиком.
class MetricTile extends StatelessWidget {
  const MetricTile({
    super.key,
    required this.title,
    required this.subtitle,
    required this.value,
    required this.color,
    this.onTap,
    this.isLoading = false,
  });

  final String title;
  final String subtitle;
  final String value;
  final Color color;
  final VoidCallback? onTap;
  final bool isLoading;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final cardContent = isLoading
        ? const _TileLoadingPlaceholder()
        : Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        title,
                        style: theme.textTheme.titleMedium?.copyWith(
                          color: theme.colorScheme.onSurface,
                        ),
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
                  _ProgressRing(color: color),
                ],
              ),
              const SizedBox(height: 24),
              Row(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Hero(
                    tag: 'metric-value-$title',
                    child: Text(
                      value,
                      style: theme.textTheme.displaySmall?.copyWith(
                        color: color,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(child: _Sparkline(color: color)),
                ],
              ),
            ],
          );

    return Card(
      color: theme.colorScheme.surface,
      elevation: 3,
      clipBehavior: Clip.antiAlias,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: cardContent,
        ),
      ),
    );
  }
}

class _Sparkline extends StatelessWidget {
  const _Sparkline({required this.color});

  final Color color;

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      painter: _SparklinePainter(color: color),
      size: const Size(double.infinity, 48),
    );
  }
}

class _SparklinePainter extends CustomPainter {
  _SparklinePainter({required this.color});

  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color.withOpacity(0.5)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round;

    final path = Path();
    const points = [0.1, 0.35, 0.25, 0.55, 0.4, 0.7, 0.5, 0.9];
    for (var i = 0; i < points.length; i += 2) {
      final x = points[i] * size.width;
      final y = size.height - (points[i + 1] * size.height);
      if (i == 0) {
        path.moveTo(x, y);
      } else {
        path.lineTo(x, y);
      }
    }
    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _ProgressRing extends StatelessWidget {
  const _ProgressRing({required this.color});

  final Color color;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 40,
      width: 40,
      child: TweenAnimationBuilder<double>(
        tween: Tween(begin: 0, end: 0.76),
        duration: const Duration(milliseconds: 900),
        curve: Curves.easeOutCubic,
        builder: (context, value, _) {
          return CustomPaint(
            painter: _RingPainter(progress: value, color: color),
          );
        },
      ),
    );
  }
}

class _RingPainter extends CustomPainter {
  _RingPainter({required this.progress, required this.color});

  final double progress;
  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final strokeWidth = 6.0;
    final rect = Offset.zero & size;

    final background = Paint()
      ..color = color.withOpacity(0.12)
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth;

    final foreground = Paint()
      ..shader = SweepGradient(
        startAngle: -math.pi / 2,
        endAngle: 1.5 * math.pi,
        colors: [color, color.withOpacity(0.5)],
      ).createShader(rect)
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round
      ..strokeWidth = strokeWidth;

    final center = size.center(Offset.zero);
    final radius = (size.shortestSide - strokeWidth) / 2;

    canvas.drawCircle(center, radius, background);
    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      -math.pi / 2,
      progress * 2 * math.pi,
      false,
      foreground,
    );
  }

  @override
  bool shouldRepaint(covariant _RingPainter oldDelegate) =>
      oldDelegate.progress != progress || oldDelegate.color != color;
}

class _TileLoadingPlaceholder extends StatefulWidget {
  const _TileLoadingPlaceholder();

  @override
  State<_TileLoadingPlaceholder> createState() => _TileLoadingPlaceholderState();
}

class _TileLoadingPlaceholderState extends State<_TileLoadingPlaceholder>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1400),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _ShimmerBlock(width: 120, height: 18, progress: _controller.value),
            const SizedBox(height: 8),
            _ShimmerBlock(width: 80, height: 14, progress: _controller.value),
            const SizedBox(height: 32),
            _ShimmerBlock(width: 160, height: 36, progress: _controller.value),
          ],
        );
      },
    );
  }
}

class _ShimmerBlock extends StatelessWidget {
  const _ShimmerBlock({
    required this.width,
    required this.height,
    required this.progress,
  });

  final double width;
  final double height;
  final double progress;

  @override
  Widget build(BuildContext context) {
    final baseColor = Theme.of(context).colorScheme.surfaceVariant;
    final highlight = Theme.of(context).colorScheme.onSurface.withOpacity(0.05);
    final gradient = LinearGradient(
      colors: [baseColor, highlight, baseColor],
      stops: [0, 0.5, 1],
      transform: _SlidingGradientTransform(progress),
    );

    return Container(
      width: width,
      height: height,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(12),
        gradient: gradient,
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
