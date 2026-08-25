-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 13-08-2026 a las 21:56:19
-- Versión del servidor: 8.0.44
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `upgrade_erp`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cargo_colaborador`
--

CREATE TABLE `cargo_colaborador` (
  `id` bigint NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `cargo_colaborador`
--

INSERT INTO `cargo_colaborador` (`id`, `nombre`, `descripcion`, `activo`) VALUES
(1, 'Administrador', 'Administración general del sistema', 1),
(2, 'Coordinadora de producción', 'Coordinación de producciones y eventos', 1),
(3, 'Diseñadora visual', 'Diseño y contenido visual', 1),
(4, 'Ejecutivo comercial', 'Gestión comercial y atención de clientes', 1),
(5, 'Iluminadora', 'Operación y diseño de iluminación', 1),
(6, 'Operador LED', 'Operación de pantallas y video LED', 1),
(7, 'Roadie / Montajista', 'Montaje, desmontaje y apoyo de escenario', 1),
(8, 'Técnico de sonido', 'Operación técnica de audio', 1),
(9, 'Técnico de sonido senior', 'Operación senior de audio', 1),
(10, 'Sin definir', 'Cargo temporal para registros migrados', 1),
(11, 'Tecnico', 'Migrado desde usuario', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categoria_galeria`
--

CREATE TABLE `categoria_galeria` (
  `id` bigint NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `categoria_galeria`
--

INSERT INTO `categoria_galeria` (`id`, `nombre`, `activa`, `fecha_creacion`) VALUES
(1, 'Eventos en Vivo', 1, '2026-08-04 00:55:15'),
(2, 'Pantallas LED', 1, '2026-08-04 00:55:15'),
(3, 'Eventos Corporativos', 1, '2026-08-04 00:55:15'),
(4, 'Sistemas de Iluminación', 1, '2026-08-04 00:55:15'),
(5, 'Audio Profesional', 1, '2026-08-04 00:55:15'),
(6, 'Producción Audiovisual', 1, '2026-08-04 00:55:15');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categoria_inventario`
--

CREATE TABLE `categoria_inventario` (
  `id` bigint NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `icono` varchar(100) NOT NULL DEFAULT 'package',
  `tono` varchar(30) NOT NULL DEFAULT 'muted',
  `activa` tinyint(1) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `categoria_inventario`
--

INSERT INTO `categoria_inventario` (`id`, `nombre`, `descripcion`, `icono`, `tono`, `activa`) VALUES
(1, 'Audio', 'Equipos profesionales de sonido', 'headphones', 'primary', 1),
(2, 'Iluminación', 'Equipos de iluminación para eventos', 'lightbulb', 'warning', 1),
(3, 'Video', 'Equipos de producción y reproducción de video', 'monitor', 'primary', 1),
(4, 'Pantallas LED', 'Pantallas y módulos LED indoor y outdoor', 'boxes', 'primary', 1),
(5, 'Estructuras', 'Truss, tarimas y estructuras de montaje', 'package', 'muted', 1),
(6, 'Accesorios', 'Cableado, adaptadores y accesorios audiovisuales', 'cable', 'muted', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categoria_servicio`
--

CREATE TABLE `categoria_servicio` (
  `id` bigint NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `categoria_servicio`
--

INSERT INTO `categoria_servicio` (`id`, `nombre`, `descripcion`, `activa`) VALUES
(1, 'General', 'Servicios sin una clasificación específica', 1),
(2, 'Producción', 'Producción y coordinación integral de eventos', 1),
(3, 'Audio', 'Sonido profesional y operación técnica', 1),
(4, 'Iluminación', 'Diseño y operación de iluminación', 1),
(5, 'Video', 'Pantallas, cámaras y producción de video', 1),
(6, 'Entretenimiento', 'DJ, música y experiencias de entretenimiento', 1),
(7, 'Streaming', 'Transmisión en vivo y producción multicámara', 1),
(8, 'Alquiler', 'Alquiler de equipos audiovisuales', 1),
(9, 'Mantenimiento', 'Soporte técnico preventivo y correctivo', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `checklist_mantenimiento`
--

CREATE TABLE `checklist_mantenimiento` (
  `id` bigint NOT NULL,
  `orden_id` bigint NOT NULL,
  `tarea` varchar(180) NOT NULL,
  `completado` tinyint(1) NOT NULL DEFAULT '0',
  `posicion` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `checklist_mantenimiento`
--

INSERT INTO `checklist_mantenimiento` (`id`, `orden_id`, `tarea`, `completado`, `posicion`) VALUES
(1, 4, 'Inspección visual', 1, 1),
(2, 3, 'Inspección visual', 1, 1),
(3, 2, 'Inspección visual', 1, 1),
(4, 1, 'Inspección visual', 1, 1),
(5, 4, 'Diagnóstico o medición técnica', 1, 2),
(6, 3, 'Diagnóstico o medición técnica', 1, 2),
(7, 2, 'Diagnóstico o medición técnica', 1, 2),
(8, 1, 'Diagnóstico o medición técnica', 1, 2),
(9, 4, 'Reparación o limpieza', 1, 3),
(10, 3, 'Reparación o limpieza', 0, 3),
(11, 2, 'Reparación o limpieza', 1, 3),
(12, 1, 'Reparación o limpieza', 0, 3),
(13, 4, 'Prueba de funcionamiento', 1, 4),
(14, 3, 'Prueba de funcionamiento', 0, 4),
(15, 2, 'Prueba de funcionamiento', 0, 4),
(16, 1, 'Prueba de funcionamiento', 0, 4),
(17, 4, 'Validación final', 1, 5),
(18, 3, 'Validación final', 0, 5),
(19, 2, 'Validación final', 0, 5),
(20, 1, 'Validación final', 0, 5);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cliente`
--

CREATE TABLE `cliente` (
  `id` bigint NOT NULL,
  `tipo_cliente` varchar(20) NOT NULL DEFAULT 'EMPRESA',
  `identificacion` varchar(50) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `empresa` varchar(150) DEFAULT NULL,
  `correo` varchar(100) NOT NULL,
  `telefono` varchar(30) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `segmento` varchar(50) DEFAULT NULL,
  `condiciones_pago` varchar(100) DEFAULT NULL,
  `notas` text,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_registro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `cliente`
--

INSERT INTO `cliente` (`id`, `tipo_cliente`, `identificacion`, `nombre`, `empresa`, `correo`, `telefono`, `direccion`, `segmento`, `condiciones_pago`, `notas`, `activo`, `fecha_registro`) VALUES
(1, 'EMPRESA', '3-101-000001', 'Carlos Ramírez', 'Eventos CR', 'carlos@eventoscr.com', '8888-1234', 'San José, Costa Rica', 'Corporativo', '30 días', NULL, 1, '2026-07-09 14:13:35'),
(2, 'PERSONA', '118330468', 'Gabriel Cabalceta', NULL, 'cabalceta.gabriel.2001@gmail.com', '83442306', 'dfsdfsdfdsfsdfdsfsdfsd', 'Corporativo', '30', 'fsdfdsfsdfdsfsdf', 1, '2026-08-04 04:42:12');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `configuracion_usuario`
--

CREATE TABLE `configuracion_usuario` (
  `id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `idioma` varchar(10) NOT NULL DEFAULT 'es',
  `tema` varchar(50) NOT NULL DEFAULT 'system',
  `notificaciones` tinyint(1) NOT NULL DEFAULT '1',
  `zona_horaria` varchar(60) NOT NULL DEFAULT 'America/Costa_Rica',
  `densidad` varchar(20) NOT NULL DEFAULT 'comfortable',
  `biografia` varchar(500) DEFAULT NULL,
  `foto_perfil` varchar(500) DEFAULT NULL,
  `fecha_cambio_password` datetime DEFAULT NULL,
  `fecha_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `configuracion_usuario`
--

INSERT INTO `configuracion_usuario` (`id`, `usuario_id`, `idioma`, `tema`, `notificaciones`, `zona_horaria`, `densidad`, `biografia`, `foto_perfil`, `fecha_cambio_password`, `fecha_actualizacion`) VALUES
(1, 1, 'es', 'dark', 1, 'America/Costa_Rica', 'compact', NULL, NULL, NULL, '2026-08-13 18:27:20'),
(2, 6, 'es', 'dark', 1, 'America/Costa_Rica', 'comfortable', NULL, '/uploads/profile/0cf65842-126c-4bae-9a90-3fabe983ec2a.png', NULL, '2026-08-13 18:30:02'),
(3, 2, 'es', 'system', 1, 'America/Costa_Rica', 'comfortable', NULL, NULL, NULL, '2026-08-13 12:09:58'),
(4, 3, 'es', 'system', 1, 'America/Costa_Rica', 'comfortable', NULL, NULL, NULL, '2026-08-13 12:09:58'),
(5, 4, 'es', 'system', 1, 'America/Costa_Rica', 'comfortable', NULL, NULL, NULL, '2026-08-13 12:09:58'),
(6, 5, 'es', 'system', 1, 'America/Costa_Rica', 'comfortable', NULL, NULL, NULL, '2026-08-13 12:09:58'),
(9, 7, 'es', 'system', 1, 'America/Costa_Rica', 'comfortable', NULL, NULL, NULL, '2026-08-13 19:26:20');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cotizacion`
--

CREATE TABLE `cotizacion` (
  `id` bigint NOT NULL,
  `numero` varchar(50) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `descripcion` varchar(255) DEFAULT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `impuesto` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `estado` varchar(50) NOT NULL DEFAULT 'Borrador',
  `cliente_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `cotizacion`
--

INSERT INTO `cotizacion` (`id`, `numero`, `fecha`, `descripcion`, `subtotal`, `impuesto`, `total`, `estado`, `cliente_id`, `usuario_id`) VALUES
(1, 'COT-2026-0001', '2026-07-09 14:14:00', 'Cotización de prueba para evento corporativo', 250000.00, 32500.00, 282500.00, 'BORRADOR', 1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cotizacion_servicio`
--

CREATE TABLE `cotizacion_servicio` (
  `cotizacion_id` bigint NOT NULL,
  `servicio_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `cotizacion_servicio`
--

INSERT INTO `cotizacion_servicio` (`cotizacion_id`, `servicio_id`) VALUES
(1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `departamento_colaborador`
--

CREATE TABLE `departamento_colaborador` (
  `id` bigint NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `departamento_colaborador`
--

INSERT INTO `departamento_colaborador` (`id`, `nombre`, `descripcion`, `activo`) VALUES
(1, 'Administración', 'Administración y gestión interna', 1),
(2, 'Audio', 'Operación técnica de audio', 1),
(3, 'Comercial', 'Ventas y gestión de clientes', 1),
(4, 'Diseño', 'Diseño gráfico y contenido visual', 1),
(5, 'General', 'Departamento temporal para registros migrados', 1),
(6, 'Iluminación', 'Operación técnica de iluminación', 1),
(7, 'Montaje', 'Montaje y logística de escenario', 1),
(8, 'Operaciones', 'Operación técnica general', 1),
(9, 'Producción', 'Producción y coordinación de eventos', 1),
(10, 'Video y pantallas', 'Video, realización y pantallas LED', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `elemento_galeria`
--

CREATE TABLE `elemento_galeria` (
  `id` bigint NOT NULL,
  `tipo` varchar(20) NOT NULL,
  `fuente` varchar(20) NOT NULL,
  `ruta_media` varchar(500) NOT NULL,
  `ruta_portada` varchar(500) DEFAULT NULL,
  `titulo` varchar(150) NOT NULL,
  `categoria_id` bigint NOT NULL,
  `descripcion` varchar(600) DEFAULT NULL,
  `texto_alternativo` varchar(200) NOT NULL,
  `diseno` varchar(20) NOT NULL DEFAULT 'ESTANDAR',
  `publicado` tinyint(1) NOT NULL DEFAULT '1',
  `destacado` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_evento` date NOT NULL,
  `orden` int NOT NULL,
  `nombre_archivo_original` varchar(255) DEFAULT NULL,
  `tamano_bytes` bigint DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `elemento_galeria`
--

INSERT INTO `elemento_galeria` (`id`, `tipo`, `fuente`, `ruta_media`, `ruta_portada`, `titulo`, `categoria_id`, `descripcion`, `texto_alternativo`, `diseno`, `publicado`, `destacado`, `fecha_evento`, `orden`, `nombre_archivo_original`, `tamano_bytes`, `fecha_creacion`, `fecha_actualizacion`) VALUES
(1, 'IMAGEN', 'URL', 'https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?auto=format&fit=crop&w=1600&q=80', NULL, 'Festival Nocturno 2025', 1, 'Producción audiovisual integral para festival al aire libre.', 'Escenario principal del Festival Nocturno 2025', 'GRANDE', 1, 1, '2026-06-18', 1, NULL, NULL, '2026-08-04 00:55:24', '2026-08-04 07:05:18'),
(2, 'VIDEO', 'URL', 'https://cdn.coverr.co/videos/coverr-a-concert-with-many-people-2633/1080p.mp4', 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?auto=format&fit=crop&w=1200&q=80', 'Pantallas LED Outdoor', 2, 'Montaje y operación de pantallas LED para concierto masivo.', 'Pantallas LED durante un concierto', 'HORIZONTAL', 1, 1, '2026-06-12', 2, NULL, NULL, '2026-08-04 00:55:24', '2026-08-04 07:05:18'),
(3, 'IMAGEN', 'URL', 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=1200&q=80', NULL, 'Lanzamiento Corporativo', 3, 'Lanzamiento de producto con escenario, video e iluminación.', 'Evento corporativo con escenario iluminado', 'ESTANDAR', 1, 0, '2026-05-28', 3, NULL, NULL, '2026-08-04 00:55:24', '2026-08-04 00:55:24'),
(4, 'IMAGEN', 'URL', 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?auto=format&fit=crop&w=1200&q=80', NULL, 'Iluminación Escénica Premium', 4, 'Diseño de iluminación para presentación musical.', 'Artista bajo iluminación escénica', 'ESTANDAR', 1, 0, '2026-05-20', 4, NULL, NULL, '2026-08-04 00:55:24', '2026-08-04 00:55:24'),
(5, 'VIDEO', 'URL', 'https://cdn.coverr.co/videos/coverr-stage-lights-at-a-concert-7689/1080p.mp4', 'https://images.unsplash.com/photo-1429962714451-bb934ecdc4ec?auto=format&fit=crop&w=1600&q=80', 'Concierto Arena Norte', 5, 'Sistema de audio line array e iluminación para arena.', 'Escenario del Concierto Arena Norte', 'GRANDE', 1, 1, '2026-05-14', 5, NULL, NULL, '2026-08-04 00:55:24', '2026-08-04 07:07:59'),
(6, 'IMAGEN', 'URL', 'https://images.unsplash.com/photo-1505236858219-8359eb29e329?auto=format&fit=crop&w=1200&q=80', NULL, 'Convención Anual Tech', 6, 'Producción técnica completa para convención empresarial.', 'Asistentes durante una convención tecnológica', 'ESTANDAR', 1, 0, '2026-05-02', 6, NULL, NULL, '2026-08-04 00:55:24', '2026-08-04 07:06:31'),
(7, 'IMAGEN', 'URL', 'https://images.unsplash.com/photo-1531058020387-3be344556be6?auto=format&fit=crop&w=1200&q=80', NULL, 'Show de Apertura', 1, 'Diseño técnico y operación del show de apertura.', 'Público durante un show de apertura', 'ESTANDAR', 1, 0, '2026-04-26', 7, NULL, NULL, '2026-08-04 00:55:24', '2026-08-04 00:55:24'),
(8, 'IMAGEN', 'URL', 'https://images.unsplash.com/photo-1574391884720-bbc3740c59d1?auto=format&fit=crop&w=1200&q=80', NULL, 'Conferencia Internacional', 3, 'Audio, video y soporte técnico para conferencia.', 'Conferencia internacional en auditorio', 'HORIZONTAL', 1, 0, '2026-04-15', 8, NULL, NULL, '2026-08-04 00:55:24', '2026-08-04 00:55:24'),
(9, 'VIDEO', 'URL', 'https://cdn.coverr.co/videos/coverr-an-audience-at-a-concert-4665/1080p.mp4', 'https://images.unsplash.com/photo-1598387993441-a364f854c3e1?auto=format&fit=crop&w=1600&q=80', 'Wall LED 8K', 2, 'Configuración creativa de muro LED de gran formato.', 'Muro LED en evento musical', 'HORIZONTAL', 1, 0, '2026-04-08', 9, NULL, NULL, '2026-08-04 00:55:24', '2026-08-04 07:06:35'),
(10, 'IMAGEN', 'URL', 'https://images.unsplash.com/photo-1518972559570-7cc1309f3229?auto=format&fit=crop&w=1200&q=80', NULL, 'Backstage Tour Nacional', 5, 'Operación y soporte técnico durante gira nacional.', 'Backstage de un concierto', 'HORIZONTAL', 1, 0, '2026-03-29', 10, NULL, NULL, '2026-08-04 00:55:24', '2026-08-04 00:55:24');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `evidencia_mantenimiento`
--

CREATE TABLE `evidencia_mantenimiento` (
  `id` bigint NOT NULL,
  `orden_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `nombre_original` varchar(255) NOT NULL,
  `ruta_archivo` varchar(500) NOT NULL,
  `tipo_mime` varchar(100) NOT NULL,
  `tamano_bytes` bigint NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `evidencia_mantenimiento`
--

INSERT INTO `evidencia_mantenimiento` (`id`, `orden_id`, `usuario_id`, `nombre_original`, `ruta_archivo`, `tipo_mime`, `tamano_bytes`, `fecha_creacion`) VALUES
(1, 2, 1, 'ferrari-296-speciale-2025-5k-cg-1920x1080.jpg', '/uploads/maintenance/cd15d1f0-c767-4223-8b65-adcc01a18729.jpg', 'image/jpeg', 573533, '2026-08-11 05:54:10');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inventario`
--

CREATE TABLE `inventario` (
  `id` bigint NOT NULL,
  `codigo` varchar(255) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `descripcion` text,
  `numero_serie` varchar(255) DEFAULT NULL,
  `marca` varchar(100) DEFAULT NULL,
  `modelo` varchar(100) DEFAULT NULL,
  `bodega` varchar(100) DEFAULT NULL,
  `ubicacion` varchar(150) DEFAULT NULL,
  `valor_unitario` decimal(38,2) NOT NULL,
  `precio_venta` decimal(12,2) DEFAULT NULL,
  `cantidad_total` int NOT NULL,
  `cantidad_disponible` int NOT NULL,
  `estado` varchar(255) NOT NULL,
  `fecha_adquisicion` date DEFAULT NULL,
  `categoria_id` bigint NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `inventario`
--

INSERT INTO `inventario` (`id`, `codigo`, `nombre`, `descripcion`, `numero_serie`, `marca`, `modelo`, `bodega`, `ubicacion`, `valor_unitario`, `precio_venta`, `cantidad_total`, `cantidad_disponible`, `estado`, `fecha_adquisicion`, `categoria_id`, `activo`, `fecha_creacion`, `fecha_actualizacion`) VALUES
(1, 'AUD-002', 'Parlante Activo', 'Parlante profesional para eventos', 'SN-AUD-001', NULL, NULL, NULL, NULL, 850000.00, NULL, 4, 4, 'En Bodega', '2025-01-15', 1, 1, '2026-08-10 22:11:02', '2026-08-10 22:11:02'),
(3, 'AUD-100', 'Line Array L-Acoustics K2', 'Sistema line array profesional para eventos de gran formato.', 'K2-2026-0100', 'L-Acoustics', 'K2', 'Bodega A', 'Rack 12', 2400000.00, 2950000.00, 16, 14, 'En Mantenimiento', '2025-01-15', 1, 1, '2026-08-10 22:26:52', '2026-08-10 23:42:23'),
(4, 'AUD-101', 'Consola digital Yamaha CL5', 'Consola profesional para mezcla de audio en vivo.', 'YM-CL5-0101', 'Yamaha', 'CL5', 'Taller técnico', 'Mesa de diagnóstico 2', 680000.00, 825000.00, 1, 0, 'En Mantenimiento', '2024-08-10', 1, 1, '2026-08-10 22:26:52', '2026-08-10 22:26:52'),
(5, 'ILU-200', 'Cabeza móvil Robe MegaPointe', 'Luminaria híbrida para efectos beam, spot y wash.', 'RB-MP-0200', 'Robe', 'MegaPointe', 'Bodega B', 'Rack IL-04', 1250000.00, 1490000.00, 24, 14, 'En Préstamo', '2025-03-20', 2, 1, '2026-08-10 22:26:52', '2026-08-10 22:58:11'),
(6, 'ILU-201', 'Consola de iluminación grandMA3', 'Consola de control para producciones de iluminación profesional.', 'MA3-L-0201', 'MA Lighting', 'grandMA3 Light', 'Unidad móvil', 'Camión #2', 5400000.00, 6100000.00, 2, 1, 'En Camión', '2025-05-12', 2, 1, '2026-08-10 22:26:52', '2026-08-10 22:26:52'),
(7, 'VID-300', 'Cámara Sony FX9', 'Cámara cinematográfica full-frame para producciones audiovisuales.', 'SN-FX9-0300', 'Sony', 'PXW-FX9', 'Bodega A', 'Rack Video 4', 3200000.00, 3790000.00, 3, 2, 'En Préstamo', '2025-06-01', 3, 1, '2026-08-10 22:26:52', '2026-08-10 22:58:11'),
(8, 'VID-301', 'Switcher Blackmagic ATEM Television Studio', 'Switcher profesional para realización multicámara y streaming.', 'BM-ATEM-0301', 'Blackmagic Design', 'ATEM Television Studio HD8', 'Bodega A', 'Rack Video 2', 980000.00, 1190000.00, 2, 2, 'Disponible', '2025-02-18', 3, 1, '2026-08-10 22:26:52', '2026-08-10 22:26:52'),
(9, 'LED-400', 'Pantalla LED P3.9 Outdoor', 'Módulos LED para exteriores de alta luminosidad.', 'LED-P39-0400', 'Unilumin', 'Uslim P3.9', 'Unidad móvil', 'Camión #3', 79000.00, 105000.00, 48, 0, 'En Camión', '2024-11-05', 4, 1, '2026-08-10 22:26:52', '2026-08-10 22:26:52'),
(10, 'LED-401', 'Pantalla LED P2.6 Indoor', 'Paneles LED de alta resolución para espacios interiores.', 'LED-P26-0401', 'Absen', 'KL II P2.6', 'Bodega C', 'Zona LED 1', 92000.00, 118000.00, 36, 36, 'En Bodega', '2025-04-22', 4, 1, '2026-08-10 22:26:52', '2026-08-10 22:26:52'),
(11, 'EST-500', 'Truss aluminio 4 metros', 'Sección de estructura de aluminio para montajes audiovisuales.', 'GT-F34-0500', 'Global Truss', 'F34 4m', 'Bodega B', 'Zona Estructuras', 240000.00, 295000.00, 32, 32, 'En Bodega', '2024-07-08', 5, 1, '2026-08-10 22:26:52', '2026-08-10 22:26:52'),
(12, 'EST-501', 'Tarima modular 2x1 metros', 'Módulo antideslizante para escenarios y plataformas.', 'STG-2X1-0501', 'Prolyte', 'StageDex 2x1', 'Bodega B', 'Zona Tarimas', 185000.00, 230000.00, 20, 16, 'En Préstamo', '2025-01-27', 5, 1, '2026-08-10 22:26:52', '2026-08-10 22:58:11'),
(13, 'ACC-600', 'Cable XLR profesional 20 metros', 'Cable balanceado para conexión de audio profesional.', 'XLR20-0600', 'Neutrik', 'XLR Pro 20m', 'Bodega A', 'Gabinete Cables 3', 28000.00, 39000.00, 120, 90, 'Dado de baja', '2024-09-14', 6, 0, '2026-08-10 22:26:52', '2026-08-11 04:32:43'),
(14, 'ACC-601', 'Distribuidor eléctrico trifásico', 'Centro de distribución eléctrica para escenarios.', 'PWR-3F-0601', 'Motion Labs', 'RPD 63A', 'Taller técnico', 'Área eléctrica', 460000.00, 550000.00, 6, 5, 'En Mantenimiento', '2025-03-04', 6, 1, '2026-08-10 22:26:52', '2026-08-10 22:26:52');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `modulo`
--

CREATE TABLE `modulo` (
  `id` bigint NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `ruta_base` varchar(150) NOT NULL,
  `icono` varchar(100) DEFAULT NULL,
  `grupo` varchar(60) NOT NULL DEFAULT 'General',
  `orden` int NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `modulo`
--

INSERT INTO `modulo` (`id`, `nombre`, `descripcion`, `ruta_base`, `icono`, `grupo`, `orden`, `activo`) VALUES
(1, 'INVENTARIO', 'Equipos, existencias y movimientos', '/admin/inventario', 'package', 'Operación', 3, 1),
(2, 'DASHBOARD', 'Resumen ejecutivo de la operación', '/admin/dashboard', 'layout-dashboard', 'General', 1, 1),
(3, 'CLIENTES', 'Gestión completa de clientes', '/admin/clientes', 'users', 'Comercial', 2, 1),
(4, 'SERVICIOS', 'Catálogo comercial de servicios', '/admin/servicios', 'sparkles', 'Comercial', 4, 1),
(5, 'COTIZACIONES', 'Propuestas y cotizaciones comerciales', '/admin/cotizaciones', 'file-text', 'Comercial', 5, 1),
(6, 'CALENDARIO', 'Agenda operativa y eventos', '/admin/calendario', 'calendar-days', 'Operación', 6, 1),
(7, 'PRESTAMOS', 'Préstamos y devoluciones de equipos', '/admin/prestamos', 'hand-coins', 'Operación', 7, 1),
(8, 'MANTENIMIENTO', 'Órdenes y evidencias de mantenimiento', '/admin/mantenimiento', 'wrench', 'Operación', 8, 1),
(9, 'COLABORADORES', 'Personal y cuentas internas', '/admin/colaboradores', 'user-cog', 'Administración', 9, 1),
(10, 'ROLES', 'Roles y accesos por módulo', '/admin/roles', 'shield-check', 'Administración', 10, 1),
(11, 'GALERIA', 'Contenido audiovisual de la landing', '/admin/galeria', 'images', 'Contenido', 11, 1),
(12, 'PREGUNTAS', 'Preguntas frecuentes de la landing', '/admin/preguntas', 'circle-help', 'Contenido', 12, 1),
(13, 'CONFIGURACION', 'Perfil y preferencias de cuenta', '/admin/configuracion', 'settings', 'Administración', 13, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `movimiento_inventario`
--

CREATE TABLE `movimiento_inventario` (
  `id` bigint NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `fecha_movimiento` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_id` bigint NOT NULL,
  `inventario_id` bigint NOT NULL,
  `cantidad_afectada` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `movimiento_inventario`
--

INSERT INTO `movimiento_inventario` (`id`, `tipo`, `descripcion`, `fecha_movimiento`, `usuario_id`, `inventario_id`, `cantidad_afectada`) VALUES
(1, 'REGISTRO', 'Registro inicial del equipo en inventario', '2026-07-09 14:13:49', 1, 1, 4),
(2, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 3, 16),
(3, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 4, 1),
(4, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 5, 24),
(5, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 6, 2),
(6, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 7, 3),
(7, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 8, 2),
(8, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 9, 48),
(9, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 10, 36),
(10, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 11, 32),
(11, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 12, 20),
(12, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 13, 120),
(13, 'REGISTRO', 'Carga inicial de datos demostrativos.', '2026-08-10 22:27:12', 1, 14, 6),
(17, 'CAMBIO_ESTADO', 'Estado: En Préstamo → Disponible. Motivo: fdsfsdfs', '2026-08-11 04:31:55', 1, 13, 0),
(18, 'BAJA', 'El equipo fue dado de baja del inventario.', '2026-08-11 04:32:43', 1, 13, -120),
(19, 'INICIO_MANTENIMIENTO', 'Inicio de la orden demostrativa MNT-2001.', '2026-08-10 23:42:27', 1, 3, 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `observacion_mantenimiento`
--

CREATE TABLE `observacion_mantenimiento` (
  `id` bigint NOT NULL,
  `orden_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `texto` text NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `observacion_mantenimiento`
--

INSERT INTO `observacion_mantenimiento` (`id`, `orden_id`, `usuario_id`, `texto`, `fecha_creacion`) VALUES
(1, 1, 1, 'Se inició el diagnóstico y se documentaron los primeros hallazgos.', '2026-08-10 23:42:19'),
(2, 1, 1, 'Estoy en eso', '2026-08-11 05:53:28');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `orden_mantenimiento`
--

CREATE TABLE `orden_mantenimiento` (
  `id` bigint NOT NULL,
  `numero` varchar(50) DEFAULT NULL,
  `inventario_id` bigint NOT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `tecnico_id` bigint DEFAULT NULL,
  `creado_por_id` bigint NOT NULL,
  `tipo` varchar(30) NOT NULL,
  `prioridad` varchar(30) NOT NULL,
  `estado` varchar(30) NOT NULL DEFAULT 'PROGRAMADO',
  `ubicacion` varchar(180) DEFAULT NULL,
  `fecha_programada` date NOT NULL,
  `hora_programada` time NOT NULL,
  `costo_estimado` decimal(12,2) NOT NULL DEFAULT '0.00',
  `duracion_estimada` decimal(6,2) NOT NULL DEFAULT '1.00',
  `descripcion` text NOT NULL,
  `resumen_cierre` text,
  `horas_reales` decimal(6,2) DEFAULT NULL,
  `proxima_revision` date DEFAULT NULL,
  `fecha_cierre` datetime DEFAULT NULL,
  `estado_inventario_previo` varchar(40) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ;

--
-- Volcado de datos para la tabla `orden_mantenimiento`
--

INSERT INTO `orden_mantenimiento` (`id`, `numero`, `inventario_id`, `cliente_id`, `tecnico_id`, `creado_por_id`, `tipo`, `prioridad`, `estado`, `ubicacion`, `fecha_programada`, `hora_programada`, `costo_estimado`, `duracion_estimada`, `descripcion`, `resumen_cierre`, `horas_reales`, `proxima_revision`, `fecha_cierre`, `estado_inventario_previo`, `activo`, `fecha_creacion`, `fecha_actualizacion`) VALUES
(1, 'MNT-2001', 3, NULL, 2, 1, 'CORRECTIVO', 'ALTA', 'EN_PROCESO', 'Taller técnico', '2026-08-09', '09:00:00', 12400.00, 6.00, 'Diagnóstico y reparación de fallos intermitentes en canales de entrada.', NULL, NULL, NULL, NULL, 'EN_PRESTAMO', 1, '2026-08-10 23:41:49', '2026-08-10 23:41:49'),
(2, 'MNT-2002', 5, NULL, 3, 1, 'PREVENTIVO', 'MEDIA', 'PROGRAMADO', 'Bodega B', '2026-08-17', '10:00:00', 3200.00, 3.00, 'Mantenimiento preventivo, calibración y limpieza del sistema óptico.', NULL, NULL, NULL, NULL, NULL, 1, '2026-08-10 23:41:54', '2026-08-10 23:41:54'),
(3, 'MNT-2003', 10, 1, 2, 1, 'EMERGENCIA', 'CRITICA', 'PROGRAMADO', 'Instalaciones del cliente', '2026-08-07', '07:30:00', 28500.00, 8.00, 'Revisión urgente de módulos con pérdida de señal antes del evento.', NULL, NULL, NULL, NULL, NULL, 1, '2026-08-10 23:42:01', '2026-08-10 23:42:01'),
(4, 'MNT-2004', 8, NULL, 4, 1, 'PREVENTIVO', 'BAJA', 'FINALIZADO', 'Bodega A', '2026-07-31', '08:00:00', 1800.00, 2.00, 'Limpieza interna, revisión de conexiones y prueba de carga.', 'Equipo operando dentro de parámetros normales.', 2.00, '2026-11-08', '2026-08-01 23:42:08', NULL, 1, '2026-08-10 23:42:08', '2026-08-10 23:42:08');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `permiso`
--

CREATE TABLE `permiso` (
  `id` bigint NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `modulo` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `permiso`
--

INSERT INTO `permiso` (`id`, `nombre`, `descripcion`, `modulo`) VALUES
(1, 'GESTIONAR_INVENTARIO', 'Permite crear, editar y consultar equipos de inventario', 'INVENTARIO');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pregunta_frecuente`
--

CREATE TABLE `pregunta_frecuente` (
  `id` bigint NOT NULL,
  `pregunta` varchar(160) NOT NULL,
  `respuesta` varchar(600) NOT NULL,
  `categoria` varchar(30) NOT NULL DEFAULT 'GENERAL',
  `activa` tinyint(1) NOT NULL DEFAULT '1',
  `orden` int NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `pregunta_frecuente`
--

INSERT INTO `pregunta_frecuente` (`id`, `pregunta`, `respuesta`, `categoria`, `activa`, `orden`, `fecha_creacion`, `fecha_actualizacion`) VALUES
(1, '¿Qué servicios ofrece Upgrade?', 'Brindamos producción audiovisual integral: sonido profesional, iluminación escénica, pantallas LED indoor y outdoor, video en vivo, estructuras, backline y coordinación técnica para eventos corporativos, sociales y masivos.', 'SERVICIOS', 1, 2, '2026-08-03 23:31:28', '2026-08-04 05:44:19'),
(2, '¿Trabajan eventos corporativos y privados?', 'Sí. Producimos lanzamientos de marca, convenciones, galas, conferencias, activaciones, conciertos, bodas y eventos privados de alto perfil, adaptando el despliegue técnico a cada formato.', 'SERVICIOS', 1, 1, '2026-08-03 23:31:28', '2026-08-04 05:53:08'),
(3, '¿Pueden alquilar únicamente equipos audiovisuales?', 'Por supuesto. Ofrecemos alquiler de equipos con o sin operador. Nuestro inventario incluye consolas digitales, line array, cabezas móviles, pantallas LED, cámaras, switchers y más.', 'EQUIPOS', 1, 3, '2026-08-03 23:31:28', '2026-08-03 23:31:28'),
(4, '¿Incluyen personal técnico durante el evento?', 'Sí. Nuestros paquetes incluyen ingenieros de audio, iluminadores, operadores de video y stage managers certificados, presentes durante montaje, función y desmontaje.', 'OPERACION', 1, 4, '2026-08-03 23:31:28', '2026-08-03 23:31:28'),
(5, '¿Con cuánto tiempo debo solicitar una cotización?', 'Recomendamos un mínimo de 2 a 3 semanas de anticipación para eventos estándar y de 6 a 8 semanas para producciones de gran escala. Atendemos urgencias según disponibilidad.', 'CONTRATACION', 1, 5, '2026-08-03 23:31:28', '2026-08-03 23:31:28'),
(6, '¿Realizan eventos fuera del Gran Área Metropolitana?', 'Sí. Movilizamos equipos y staff a nivel nacional e internacional. Incluimos logística de transporte, hospedaje y permisos en la propuesta cuando corresponde.', 'LOGISTICA', 1, 6, '2026-08-03 23:31:28', '2026-08-03 23:31:28'),
(7, '¿Qué tipo de pantallas LED ofrecen?', 'Contamos con pantallas LED indoor (P2.6, P3.9) y outdoor (P4.8, P6) en distintos tamaños y configuraciones, ideales para escenarios, fachadas, video walls y formatos creativos.', 'EQUIPOS', 1, 7, '2026-08-03 23:31:28', '2026-08-03 23:31:28'),
(8, '¿Pueden cubrir eventos de varios días?', 'Sí. Diseñamos producciones de múltiples jornadas con guardias técnicas, mantenimiento entre funciones y respaldo de equipos críticos para garantizar continuidad operativa.', 'OPERACION', 1, 8, '2026-08-03 23:31:28', '2026-08-03 23:31:28'),
(9, '¿Cómo funciona el proceso de contratación?', 'Iniciamos con un briefing, seguimos con visita técnica si aplica, entregamos propuesta y cotización, firmamos contrato con anticipo y coordinamos producción hasta el día del evento.', 'CONTRATACION', 1, 9, '2026-08-03 23:31:28', '2026-08-03 23:31:28'),
(10, '¿Qué sucede si necesito soporte durante el evento?', 'Asignamos un líder de proyecto y staff técnico in situ durante todo el evento. Además contamos con equipos de respaldo y protocolos de contingencia ante cualquier eventualidad.', 'OPERACION', 1, 10, '2026-08-03 23:31:28', '2026-08-03 23:31:28'),
(11, '¿Realizan visitas técnicas previas?', 'Sí. Para eventos que lo requieran realizamos visita al venue para validar accesos, cargas eléctricas, puntos de rigging y coordinación con el recinto.', 'LOGISTICA', 1, 11, '2026-08-03 23:31:28', '2026-08-03 23:31:28'),
(12, '¿Ofrecen paquetes personalizados?', 'Cada propuesta se diseña a la medida del evento, su audiencia y presupuesto. No trabajamos con paquetes rígidos: ajustamos equipos, escala y staff a tu necesidad real.', 'SERVICIOS', 1, 12, '2026-08-03 23:31:28', '2026-08-03 23:31:28');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `prestamo`
--

CREATE TABLE `prestamo` (
  `id` bigint NOT NULL,
  `folio` varchar(50) DEFAULT NULL,
  `cliente_id` bigint NOT NULL,
  `inventario_id` bigint NOT NULL,
  `responsable_id` bigint NOT NULL,
  `correo_contacto` varchar(120) NOT NULL,
  `cantidad` int NOT NULL,
  `fecha_salida` date NOT NULL,
  `fecha_devolucion_estimada` date NOT NULL,
  `fecha_devolucion_real` date DEFAULT NULL,
  `estado` varchar(30) NOT NULL DEFAULT 'ACTIVO',
  `condicion_salida` varchar(40) NOT NULL,
  `condicion_devolucion` varchar(40) DEFAULT NULL,
  `observaciones` text,
  `observaciones_devolucion` text,
  `ultima_notificacion_vencimiento` datetime DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ;

--
-- Volcado de datos para la tabla `prestamo`
--

INSERT INTO `prestamo` (`id`, `folio`, `cliente_id`, `inventario_id`, `responsable_id`, `correo_contacto`, `cantidad`, `fecha_salida`, `fecha_devolucion_estimada`, `fecha_devolucion_real`, `estado`, `condicion_salida`, `condicion_devolucion`, `observaciones`, `observaciones_devolucion`, `ultima_notificacion_vencimiento`, `fecha_creacion`, `fecha_actualizacion`) VALUES
(1, 'PR-2001', 1, 3, 1, 'carlos@eventoscr.com', 2, '2026-08-08', '2026-08-17', NULL, 'ACTIVO', 'OPTIMO', NULL, 'Incluye cableado y estuches de transporte.', NULL, NULL, '2026-08-10 22:57:40', '2026-08-10 22:57:40'),
(2, 'PR-2002', 1, 7, 1, 'carlos@eventoscr.com', 1, '2026-07-29', '2026-08-05', NULL, 'ACTIVO', 'CON_DETALLES', NULL, 'Préstamo vencido para probar las alertas por correo.', NULL, '2026-08-11 05:06:18', '2026-08-10 22:57:48', '2026-08-11 05:06:18'),
(3, 'PR-2003', 1, 12, 1, 'carlos@eventoscr.com', 4, '2026-08-07', '2026-08-10', NULL, 'ACTIVO', 'OPTIMO', NULL, 'Devolución programada para hoy.', NULL, NULL, '2026-08-10 22:57:53', '2026-08-10 22:57:53'),
(4, 'PR-2004', 1, 8, 1, 'carlos@eventoscr.com', 2, '2026-07-26', '2026-08-02', '2026-08-03', 'DEVUELTO', 'OPTIMO', 'OPTIMO', 'Préstamo completado.', 'Devolución completa y sin daños.', NULL, '2026-08-10 22:57:57', '2026-08-10 22:57:57'),
(5, 'PR-2005', 1, 5, 1, 'carlos@eventoscr.com', 10, '2026-08-10', '2026-08-24', NULL, 'ACTIVO', 'RECIEN_REPARADO', NULL, 'Incluye clamps y cables de seguridad.', NULL, NULL, '2026-08-10 22:58:01', '2026-08-10 22:58:01');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rol`
--

CREATE TABLE `rol` (
  `id` bigint NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `nombre_mostrado` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `protegido` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `rol`
--

INSERT INTO `rol` (`id`, `nombre`, `nombre_mostrado`, `descripcion`, `activo`, `protegido`, `fecha_creacion`) VALUES
(1, 'ADMIN', 'Administrador', 'Administrador del sistema', 1, 1, '2026-08-13 13:16:22'),
(2, 'BODEGUERO', 'Bodeguero', 'Encargado de inventario y préstamos', 1, 0, '2026-08-13 13:16:22'),
(3, 'EJECUTIVO_VENTAS', 'Ejecutivo de ventas', 'Encargado de clientes y cotizaciones', 1, 0, '2026-08-13 13:16:22'),
(4, 'GERENTE_OPERATIVO', 'Gerente operativo', 'Responsable de la operación general', 1, 0, '2026-08-13 13:16:22'),
(5, 'COORDINADOR', 'Coordinador', 'Coordinación de equipos, eventos y personal', 1, 0, '2026-08-13 13:16:22'),
(6, 'TECNICO', 'Técnico', 'Personal técnico y operativo', 1, 0, '2026-08-13 13:16:22'),
(7, 'COMERCIAL', 'Comercial', 'Gestión comercial, clientes y cotizaciones', 1, 0, '2026-08-13 13:16:22'),
(8, 'AUDITOR', 'Auditor', 'Acceso de consulta y auditoría', 1, 0, '2026-08-13 13:16:22');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rol_modulo`
--

CREATE TABLE `rol_modulo` (
  `rol_id` bigint NOT NULL,
  `modulo_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `rol_modulo`
--

INSERT INTO `rol_modulo` (`rol_id`, `modulo_id`) VALUES
(1, 1),
(2, 1),
(4, 1),
(5, 1),
(6, 1),
(1, 2),
(2, 2),
(3, 2),
(4, 2),
(5, 2),
(6, 2),
(7, 2),
(8, 2),
(1, 3),
(3, 3),
(4, 3),
(7, 3),
(1, 4),
(3, 4),
(4, 4),
(7, 4),
(1, 5),
(3, 5),
(4, 5),
(7, 5),
(1, 6),
(3, 6),
(4, 6),
(5, 6),
(7, 6),
(1, 7),
(2, 7),
(4, 7),
(5, 7),
(6, 7),
(1, 8),
(2, 8),
(4, 8),
(5, 8),
(6, 8),
(1, 9),
(4, 9),
(5, 9),
(1, 10),
(1, 11),
(4, 11),
(1, 12),
(4, 12),
(1, 13),
(4, 13);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rol_permiso`
--

CREATE TABLE `rol_permiso` (
  `rol_id` bigint NOT NULL,
  `permiso_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `rol_permiso`
--

INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES
(1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `servicio`
--

CREATE TABLE `servicio` (
  `id` bigint NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `descripcion` varchar(1000) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `categoria_id` bigint NOT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'ACTIVO',
  `unidad` varchar(30) NOT NULL DEFAULT 'SERVICIO',
  `duracion_estimada` varchar(80) DEFAULT NULL,
  `equipos_requeridos` int NOT NULL DEFAULT '0',
  `servicios_ytd` int NOT NULL DEFAULT '0',
  `visible_landing` tinyint(1) NOT NULL DEFAULT '1',
  `icono` varchar(50) NOT NULL DEFAULT 'sparkles',
  `orden` int NOT NULL DEFAULT '0',
  `eliminado` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `precio_base` decimal(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `servicio`
--

INSERT INTO `servicio` (`id`, `nombre`, `descripcion`, `activo`, `categoria_id`, `estado`, `unidad`, `duracion_estimada`, `equipos_requeridos`, `servicios_ytd`, `visible_landing`, `icono`, `orden`, `eliminado`, `fecha_creacion`, `fecha_actualizacion`, `precio_base`) VALUES
(1, 'Producción de Audio', 'Servicio profesional de producción y soporte de audio para eventos', 1, 1, 'ACTIVO', 'SERVICIO', NULL, 0, 0, 1, 'sparkles', 2, 0, '2026-08-12 00:32:05', '2026-08-12 06:43:22', 250000.00),
(2, 'Producción audiovisual integral', 'Coordinación técnica y producción completa para eventos corporativos, sociales y masivos.', 1, 2, 'ACTIVO', 'SERVICIO', '8-12 horas', 24, 32, 0, 'sparkles', 1, 0, '2026-08-12 00:32:41', '2026-08-12 06:44:24', 85000.00),
(3, 'Sonido para conferencias', 'Sistema PA profesional, seis micrófonos, consola digital y operación técnica.', 1, 3, 'ACTIVO', 'SERVICIO', '4-6 horas', 12, 58, 1, 'headphones', 3, 0, '2026-08-12 00:32:41', '2026-08-12 00:33:00', 18500.00),
(4, 'Iluminación arquitectónica', 'Diseño lumínico para realce de fachadas, escenarios y espacios de marca.', 1, 4, 'ACTIVO', 'SERVICIO', '6-8 horas', 18, 41, 1, 'lightbulb', 4, 0, '2026-08-12 00:32:41', '2026-08-12 00:33:00', 32000.00),
(5, 'Pantalla LED 6x4m', 'Pantalla indoor Full HD con estructura, procesamiento de video y operación.', 1, 5, 'ACTIVO', 'JORNADA', '6 horas', 14, 37, 1, 'monitor', 5, 0, '2026-08-12 00:32:41', '2026-08-12 00:33:00', 42000.00),
(6, 'DJ y sonido para boda', 'Cinco horas de DJ, sonido profesional e iluminación ambiental.', 1, 6, 'ACTIVO', 'EVENTO', '5 horas', 8, 29, 1, 'music', 6, 0, '2026-08-12 00:32:41', '2026-08-12 00:33:00', 24000.00),
(7, 'Streaming multicámara', 'Tres cámaras, switcher, gráficas, plataforma de transmisión y operadores.', 0, 7, 'INACTIVO', 'SERVICIO', '4-8 horas', 16, 14, 0, 'radio', 7, 0, '2026-08-12 00:32:41', '2026-08-12 00:33:00', 56000.00),
(8, 'Alquiler de equipos audiovisuales', 'Selección de equipos profesionales con opción de transporte y operador.', 1, 8, 'ACTIVO', 'JORNADA', 'Según requerimiento', 6, 23, 1, 'package', 8, 0, '2026-08-12 00:32:41', '2026-08-12 00:33:00', 15000.00),
(9, 'Mantenimiento técnico profesional', 'Diagnóstico y mantenimiento preventivo o correctivo para equipos audiovisuales.', 1, 9, 'BORRADOR', 'SERVICIO', '2-4 horas', 2, 9, 0, 'wrench', 9, 0, '2026-08-12 00:32:41', '2026-08-12 00:33:00', 12000.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
  `id` bigint NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `telefono` varchar(30) DEFAULT NULL,
  `cargo_id` bigint NOT NULL,
  `departamento_id` bigint NOT NULL,
  `estado_colaborador` varchar(30) NOT NULL DEFAULT 'DISPONIBLE',
  `eventos_asignados` int NOT NULL DEFAULT '0',
  `fecha_ingreso` date NOT NULL DEFAULT (curdate()),
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `usuario`
--

INSERT INTO `usuario` (`id`, `username`, `password`, `email`, `nombre`, `apellido`, `activo`, `telefono`, `cargo_id`, `departamento_id`, `estado_colaborador`, `eventos_asignados`, `fecha_ingreso`, `fecha_creacion`, `fecha_actualizacion`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'admin@upgrade.com', 'Administrador', 'Upgrade', 1, NULL, 10, 5, 'DISPONIBLE', 0, '2026-07-09', '2026-07-09 14:13:00', '2026-08-11 01:51:05'),
(2, 'carlos.perez', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'carlos@upgrade.com', 'Carlos', 'Pérez', 0, NULL, 2, 5, 'DISPONIBLE', 0, '2026-08-10', '2026-08-10 23:41:34', '2026-08-13 19:24:19'),
(3, 'ana.lopez', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'ana.lopez@upgrade.demo', 'Ana', 'López', 0, NULL, 10, 5, 'DISPONIBLE', 0, '2026-08-10', '2026-08-10 23:41:38', '2026-08-13 19:24:25'),
(4, 'miguel.reyes', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'miguel.reyes@upgrade.demo', 'Miguel', 'Reyes', 0, NULL, 10, 5, 'DISPONIBLE', 0, '2026-08-10', '2026-08-10 23:41:41', '2026-08-11 01:51:05'),
(5, 'gabriel', '$2a$10$0ShTIW3.TAFVYNKZ7WLl..4TclkBMRqHifS3nL1wAfCyYoU7EYIeu', 'gabriel@upgrade.com', 'Gabriel', 'Cabalceta', 0, '83442305', 11, 8, 'DISPONIBLE', 0, '2026-08-11', '2026-08-11 07:27:11', '2026-08-11 08:09:17'),
(6, 'gabriel17', '$2a$10$u99ZYuEGEZCmyCU/afis6ebxRccULO3yDRlbyxhjhpDwPnx4lBI96', 'gabriel17@upgrade.com', 'Gabriel', 'Cabalceta', 1, '83442305', 1, 2, 'DISPONIBLE', 0, '2026-08-11', '2026-08-11 08:11:48', '2026-08-11 08:11:48'),
(7, 'prueba', '$2a$10$2rcj14tg/WvySSgLj/ebluQ2gkhzWPVCZxTOn46t/83QBis05GGG2', 'prueba@upgrade.com', 'Prueba', 'Demo', 1, NULL, 3, 4, 'DISPONIBLE', 0, '2026-08-13', '2026-08-13 19:25:24', '2026-08-13 19:25:24');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario_rol`
--

CREATE TABLE `usuario_rol` (
  `usuario_id` bigint NOT NULL,
  `rol_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `usuario_rol`
--

INSERT INTO `usuario_rol` (`usuario_id`, `rol_id`) VALUES
(1, 1),
(5, 1),
(6, 1),
(2, 6),
(7, 6);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `cargo_colaborador`
--
ALTER TABLE `cargo_colaborador`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `categoria_galeria`
--
ALTER TABLE `categoria_galeria`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_categoria_galeria_nombre` (`nombre`);

--
-- Indices de la tabla `categoria_inventario`
--
ALTER TABLE `categoria_inventario`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `categoria_servicio`
--
ALTER TABLE `categoria_servicio`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `checklist_mantenimiento`
--
ALTER TABLE `checklist_mantenimiento`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_checklist_orden_posicion` (`orden_id`,`posicion`);

--
-- Indices de la tabla `cliente`
--
ALTER TABLE `cliente`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `correo` (`correo`),
  ADD UNIQUE KEY `uk_cliente_identificacion` (`identificacion`);

--
-- Indices de la tabla `configuracion_usuario`
--
ALTER TABLE `configuracion_usuario`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `usuario_id` (`usuario_id`);

--
-- Indices de la tabla `cotizacion`
--
ALTER TABLE `cotizacion`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero` (`numero`),
  ADD KEY `cliente_id` (`cliente_id`),
  ADD KEY `usuario_id` (`usuario_id`);

--
-- Indices de la tabla `cotizacion_servicio`
--
ALTER TABLE `cotizacion_servicio`
  ADD PRIMARY KEY (`cotizacion_id`,`servicio_id`),
  ADD KEY `servicio_id` (`servicio_id`);

--
-- Indices de la tabla `departamento_colaborador`
--
ALTER TABLE `departamento_colaborador`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `elemento_galeria`
--
ALTER TABLE `elemento_galeria`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_elemento_galeria_titulo` (`titulo`),
  ADD KEY `idx_elemento_galeria_publicacion_orden` (`publicado`,`orden`),
  ADD KEY `idx_elemento_galeria_categoria` (`categoria_id`),
  ADD KEY `idx_elemento_galeria_tipo` (`tipo`);

--
-- Indices de la tabla `evidencia_mantenimiento`
--
ALTER TABLE `evidencia_mantenimiento`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_evidencia_usuario` (`usuario_id`),
  ADD KEY `idx_evidencia_mantenimiento_fecha` (`orden_id`,`fecha_creacion`);

--
-- Indices de la tabla `inventario`
--
ALTER TABLE `inventario`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codigo` (`codigo`),
  ADD UNIQUE KEY `numero_serie` (`numero_serie`),
  ADD KEY `idx_inventario_activo_estado` (`activo`,`estado`),
  ADD KEY `idx_inventario_categoria_activo` (`categoria_id`,`activo`);

--
-- Indices de la tabla `modulo`
--
ALTER TABLE `modulo`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `movimiento_inventario`
--
ALTER TABLE `movimiento_inventario`
  ADD PRIMARY KEY (`id`),
  ADD KEY `usuario_id` (`usuario_id`),
  ADD KEY `idx_movimiento_inventario_fecha` (`inventario_id`,`fecha_movimiento`);

--
-- Indices de la tabla `observacion_mantenimiento`
--
ALTER TABLE `observacion_mantenimiento`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_observacion_usuario` (`usuario_id`),
  ADD KEY `idx_observacion_mantenimiento_fecha` (`orden_id`,`fecha_creacion`);

--
-- Indices de la tabla `orden_mantenimiento`
--
ALTER TABLE `orden_mantenimiento`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero` (`numero`),
  ADD KEY `fk_mantenimiento_cliente` (`cliente_id`),
  ADD KEY `fk_mantenimiento_creador` (`creado_por_id`),
  ADD KEY `idx_mantenimiento_estado_fecha` (`activo`,`estado`,`fecha_programada`),
  ADD KEY `idx_mantenimiento_inventario` (`inventario_id`,`activo`),
  ADD KEY `idx_mantenimiento_tecnico` (`tecnico_id`,`activo`);

--
-- Indices de la tabla `permiso`
--
ALTER TABLE `permiso`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `pregunta_frecuente`
--
ALTER TABLE `pregunta_frecuente`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_pregunta_frecuente_pregunta` (`pregunta`),
  ADD KEY `idx_pregunta_frecuente_publicacion` (`activa`,`orden`),
  ADD KEY `idx_pregunta_frecuente_categoria` (`categoria`);

--
-- Indices de la tabla `prestamo`
--
ALTER TABLE `prestamo`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `folio` (`folio`),
  ADD KEY `fk_prestamo_responsable` (`responsable_id`),
  ADD KEY `idx_prestamo_estado_fecha` (`estado`,`fecha_devolucion_estimada`),
  ADD KEY `idx_prestamo_cliente` (`cliente_id`),
  ADD KEY `idx_prestamo_inventario` (`inventario_id`);

--
-- Indices de la tabla `rol`
--
ALTER TABLE `rol`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `rol_modulo`
--
ALTER TABLE `rol_modulo`
  ADD PRIMARY KEY (`rol_id`,`modulo_id`),
  ADD KEY `modulo_id` (`modulo_id`);

--
-- Indices de la tabla `rol_permiso`
--
ALTER TABLE `rol_permiso`
  ADD PRIMARY KEY (`rol_id`,`permiso_id`),
  ADD KEY `permiso_id` (`permiso_id`);

--
-- Indices de la tabla `servicio`
--
ALTER TABLE `servicio`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`),
  ADD KEY `idx_servicio_estado_publicacion` (`eliminado`,`estado`,`visible_landing`,`orden`),
  ADD KEY `idx_servicio_categoria` (`categoria_id`);

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_usuario_cargo` (`cargo_id`),
  ADD KEY `idx_usuario_departamento` (`departamento_id`);

--
-- Indices de la tabla `usuario_rol`
--
ALTER TABLE `usuario_rol`
  ADD PRIMARY KEY (`usuario_id`,`rol_id`),
  ADD KEY `rol_id` (`rol_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `cargo_colaborador`
--
ALTER TABLE `cargo_colaborador`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT de la tabla `categoria_galeria`
--
ALTER TABLE `categoria_galeria`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `categoria_inventario`
--
ALTER TABLE `categoria_inventario`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `categoria_servicio`
--
ALTER TABLE `categoria_servicio`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `checklist_mantenimiento`
--
ALTER TABLE `checklist_mantenimiento`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT de la tabla `cliente`
--
ALTER TABLE `cliente`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `configuracion_usuario`
--
ALTER TABLE `configuracion_usuario`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `cotizacion`
--
ALTER TABLE `cotizacion`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `departamento_colaborador`
--
ALTER TABLE `departamento_colaborador`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT de la tabla `elemento_galeria`
--
ALTER TABLE `elemento_galeria`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT de la tabla `evidencia_mantenimiento`
--
ALTER TABLE `evidencia_mantenimiento`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `inventario`
--
ALTER TABLE `inventario`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `modulo`
--
ALTER TABLE `modulo`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `movimiento_inventario`
--
ALTER TABLE `movimiento_inventario`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT de la tabla `observacion_mantenimiento`
--
ALTER TABLE `observacion_mantenimiento`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `orden_mantenimiento`
--
ALTER TABLE `orden_mantenimiento`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `permiso`
--
ALTER TABLE `permiso`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `pregunta_frecuente`
--
ALTER TABLE `pregunta_frecuente`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT de la tabla `prestamo`
--
ALTER TABLE `prestamo`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `rol`
--
ALTER TABLE `rol`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `servicio`
--
ALTER TABLE `servicio`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `checklist_mantenimiento`
--
ALTER TABLE `checklist_mantenimiento`
  ADD CONSTRAINT `fk_checklist_orden` FOREIGN KEY (`orden_id`) REFERENCES `orden_mantenimiento` (`id`);

--
-- Filtros para la tabla `configuracion_usuario`
--
ALTER TABLE `configuracion_usuario`
  ADD CONSTRAINT `configuracion_usuario_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`);

--
-- Filtros para la tabla `cotizacion`
--
ALTER TABLE `cotizacion`
  ADD CONSTRAINT `cotizacion_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`),
  ADD CONSTRAINT `cotizacion_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`);

--
-- Filtros para la tabla `cotizacion_servicio`
--
ALTER TABLE `cotizacion_servicio`
  ADD CONSTRAINT `cotizacion_servicio_ibfk_1` FOREIGN KEY (`cotizacion_id`) REFERENCES `cotizacion` (`id`),
  ADD CONSTRAINT `cotizacion_servicio_ibfk_2` FOREIGN KEY (`servicio_id`) REFERENCES `servicio` (`id`);

--
-- Filtros para la tabla `elemento_galeria`
--
ALTER TABLE `elemento_galeria`
  ADD CONSTRAINT `fk_elemento_galeria_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categoria_galeria` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

--
-- Filtros para la tabla `evidencia_mantenimiento`
--
ALTER TABLE `evidencia_mantenimiento`
  ADD CONSTRAINT `fk_evidencia_orden` FOREIGN KEY (`orden_id`) REFERENCES `orden_mantenimiento` (`id`),
  ADD CONSTRAINT `fk_evidencia_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`);

--
-- Filtros para la tabla `inventario`
--
ALTER TABLE `inventario`
  ADD CONSTRAINT `inventario_ibfk_1` FOREIGN KEY (`categoria_id`) REFERENCES `categoria_inventario` (`id`);

--
-- Filtros para la tabla `movimiento_inventario`
--
ALTER TABLE `movimiento_inventario`
  ADD CONSTRAINT `movimiento_inventario_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  ADD CONSTRAINT `movimiento_inventario_ibfk_2` FOREIGN KEY (`inventario_id`) REFERENCES `inventario` (`id`);

--
-- Filtros para la tabla `observacion_mantenimiento`
--
ALTER TABLE `observacion_mantenimiento`
  ADD CONSTRAINT `fk_observacion_orden` FOREIGN KEY (`orden_id`) REFERENCES `orden_mantenimiento` (`id`),
  ADD CONSTRAINT `fk_observacion_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`);

--
-- Filtros para la tabla `orden_mantenimiento`
--
ALTER TABLE `orden_mantenimiento`
  ADD CONSTRAINT `fk_mantenimiento_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`),
  ADD CONSTRAINT `fk_mantenimiento_creador` FOREIGN KEY (`creado_por_id`) REFERENCES `usuario` (`id`),
  ADD CONSTRAINT `fk_mantenimiento_inventario` FOREIGN KEY (`inventario_id`) REFERENCES `inventario` (`id`),
  ADD CONSTRAINT `fk_mantenimiento_tecnico` FOREIGN KEY (`tecnico_id`) REFERENCES `usuario` (`id`);

--
-- Filtros para la tabla `prestamo`
--
ALTER TABLE `prestamo`
  ADD CONSTRAINT `fk_prestamo_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`),
  ADD CONSTRAINT `fk_prestamo_inventario` FOREIGN KEY (`inventario_id`) REFERENCES `inventario` (`id`),
  ADD CONSTRAINT `fk_prestamo_responsable` FOREIGN KEY (`responsable_id`) REFERENCES `usuario` (`id`);

--
-- Filtros para la tabla `rol_modulo`
--
ALTER TABLE `rol_modulo`
  ADD CONSTRAINT `rol_modulo_ibfk_1` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`id`),
  ADD CONSTRAINT `rol_modulo_ibfk_2` FOREIGN KEY (`modulo_id`) REFERENCES `modulo` (`id`);

--
-- Filtros para la tabla `rol_permiso`
--
ALTER TABLE `rol_permiso`
  ADD CONSTRAINT `rol_permiso_ibfk_1` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`id`),
  ADD CONSTRAINT `rol_permiso_ibfk_2` FOREIGN KEY (`permiso_id`) REFERENCES `permiso` (`id`);

--
-- Filtros para la tabla `servicio`
--
ALTER TABLE `servicio`
  ADD CONSTRAINT `fk_servicio_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categoria_servicio` (`id`);

--
-- Filtros para la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD CONSTRAINT `fk_usuario_cargo` FOREIGN KEY (`cargo_id`) REFERENCES `cargo_colaborador` (`id`),
  ADD CONSTRAINT `fk_usuario_departamento` FOREIGN KEY (`departamento_id`) REFERENCES `departamento_colaborador` (`id`);

--
-- Filtros para la tabla `usuario_rol`
--
ALTER TABLE `usuario_rol`
  ADD CONSTRAINT `usuario_rol_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  ADD CONSTRAINT `usuario_rol_ibfk_2` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
