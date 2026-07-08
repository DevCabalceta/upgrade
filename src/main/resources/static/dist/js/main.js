$(document).ready(function () {
    // 1. Inicializar iconos de Lucide (Local)
    lucide.createIcons();

    // Guardamos los selectores en variables para no re-buscarlos en cada scroll
    const $scrollTopBtn = $('#scroll-to-top');
    const $scrollTop = $('#scroll-top');
    const $socialBar = $('#floating-social-bar');

    // 2. Lógica para las animaciones de aparición (data-reveal)
    function reveal() {
        var windowHeight = $(window).height();
        var elementVisible = 100;
        var scrollY = $(window).scrollTop();

        $('[data-reveal]').each(function () {
            var elementTop = $(this).offset().top;

            if (elementTop < (scrollY + windowHeight - elementVisible)) {
                var delay = $(this).attr('data-reveal-delay');
                if (delay) {
                    $(this).css('transition-delay', delay + 'ms');
                }
                $(this).addClass('is-revealed');
            }
        });
    }

    // 3. Lógica para controlar los componentes flotantes (ScrollToTop y SocialBar)
    function checkFloatingComponents() {
        var scrollY = $(window).scrollTop();

        if (scrollY > 300) {
            // Mostrar Scroll To Top
            $scrollTopBtn
                .removeClass('opacity-0 translate-y-3 pointer-events-none')
                .addClass('opacity-100 translate-y-0 pointer-events-auto');

            // Mostrar Barra Social
            $socialBar
                .removeClass('opacity-0 translate-x-6 pointer-events-none')
                .addClass('opacity-100 translate-x-0');
        } else {
            // Ocultar Scroll To Top
            $scrollTopBtn
                .removeClass('opacity-100 translate-y-0 pointer-events-auto')
                .addClass('opacity-0 translate-y-3 pointer-events-none');

            // Ocultar Barra Social
            $socialBar
                .removeClass('opacity-100 translate-x-0')
                .addClass('opacity-0 translate-x-6 pointer-events-none');
        }
    }

    // 4. UN SOLO ESCUCHADOR DE SCROLL
    $(window).on('scroll', function () {
        reveal();
        checkFloatingComponents();
    });

    // Llamadas iniciales al cargar la página
    reveal();
    checkFloatingComponents();

    // 5. Evento Click para volver arriba suavemente
    $scrollTopBtn.on('click', function () {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });

    $scrollTop.on('click', function () {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });

});