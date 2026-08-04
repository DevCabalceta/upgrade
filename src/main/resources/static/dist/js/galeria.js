(function ($) {
    'use strict';

    $(function () {
        const $page = $('#page-content');
        const VIEW_KEY = 'upgrade-gallery-view';
        let searchTimer;
        let detailItem = null;

        function openModal(selector) {
            $('.gallery-modal').not(selector).find('video').each(function () { this.pause(); });
            $('.gallery-modal').addClass('hidden');
            $(selector).removeClass('hidden');
            $('body').addClass('overflow-hidden');
            lucide.createIcons();
        }

        function closeModals() {
            $('.gallery-modal').addClass('hidden');
            $('body').removeClass('overflow-hidden');
            $('#gallery-detail-content video').each(function () { this.pause(); });
        }

        function recordFrom($origin) {
            const data = $origin.closest('.gallery-record').data();
            return {
                id: String(data.galleryId || ''),
                type: String(data.galleryType || 'IMAGEN'),
                source: String(data.gallerySource || 'ARCHIVO'),
                media: String(data.galleryMedia || ''),
                poster: String(data.galleryPoster || ''),
                title: String(data.galleryTitle || ''),
                categoryId: String(data.galleryCategoryId || ''),
                category: String(data.galleryCategory || ''),
                description: String(data.galleryDescription || ''),
                alt: String(data.galleryAlt || ''),
                design: String(data.galleryDesign || 'ESTANDAR'),
                published: String(data.galleryPublished) === 'true',
                featured: String(data.galleryFeatured) === 'true',
                date: String(data.galleryDate || ''),
                order: String(data.galleryOrder || '')
            };
        }

        function escapeAttribute(value) {
            return String(value || '').replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        }

        function mediaMarkup(item, detail) {
            if (item.type === 'VIDEO') {
                return '<video src="' + escapeAttribute(item.media) + '"' + (item.poster ? ' poster="' + escapeAttribute(item.poster) + '"' : '') +
                    ' class="h-full w-full object-' + (detail ? 'contain' : 'cover') + '" controls playsinline' + (detail ? ' autoplay' : '') + '></video>';
            }
            return '<img src="' + escapeAttribute(item.media) + '" alt="' + escapeAttribute(item.alt || item.title) + '" class="h-full w-full object-' + (detail ? 'contain' : 'cover') + '">';
        }

        function formatDate(value) {
            if (!value) return '—';
            const parts = value.split('-');
            return parts.length === 3 ? parts[2] + '/' + parts[1] + '/' + parts[0] : value;
        }

        function showDetail(item) {
            detailItem = item;
            const statusClass = item.published
                ? 'border-[var(--success)]/20 bg-[var(--success)]/15 text-[var(--success)]'
                : 'border-border bg-muted text-muted-foreground';
            const html =
                '<div class="grid gap-6 lg:grid-cols-[minmax(0,1.2fr)_minmax(280px,.8fr)]">' +
                    '<div class="overflow-hidden rounded-2xl border border-border bg-black aspect-video">' + mediaMarkup(item, true) + '</div>' +
                    '<div class="space-y-5">' +
                        '<div><div class="flex flex-wrap items-center gap-2"><span class="inline-flex rounded-md border border-primary/20 bg-primary/10 px-2.5 py-1 text-xs font-semibold text-primary">' + escapeAttribute(item.category) + '</span><span class="inline-flex rounded-md border px-2.5 py-1 text-xs font-semibold ' + statusClass + '">' + (item.published ? 'Publicado' : 'Borrador') + '</span>' + (item.featured ? '<span class="inline-flex items-center gap-1 rounded-md bg-amber-400 px-2.5 py-1 text-xs font-semibold text-black"><i data-lucide="star" class="h-3 w-3 fill-current"></i>Destacado</span>' : '') + '</div><h3 class="mt-3 text-xl font-bold">' + escapeAttribute(item.title) + '</h3><p class="mt-2 text-sm leading-relaxed text-muted-foreground">' + escapeAttribute(item.description || 'Sin descripción') + '</p></div>' +
                        '<div class="grid grid-cols-2 gap-3"><div class="rounded-xl border border-border p-3"><div class="text-[10px] uppercase tracking-wider text-muted-foreground">Tipo</div><div class="mt-1 text-sm font-semibold">' + (item.type === 'VIDEO' ? 'Video' : 'Fotografía') + '</div></div><div class="rounded-xl border border-border p-3"><div class="text-[10px] uppercase tracking-wider text-muted-foreground">Diseño</div><div class="mt-1 text-sm font-semibold">' + ({ESTANDAR: 'Estándar', HORIZONTAL: 'Horizontal', GRANDE: 'Grande'}[item.design] || item.design) + '</div></div><div class="rounded-xl border border-border p-3"><div class="text-[10px] uppercase tracking-wider text-muted-foreground">Fecha</div><div class="mt-1 text-sm font-semibold">' + formatDate(item.date) + '</div></div><div class="rounded-xl border border-border p-3"><div class="text-[10px] uppercase tracking-wider text-muted-foreground">Orden</div><div class="mt-1 text-sm font-semibold">#' + escapeAttribute(item.order) + '</div></div></div>' +
                        '<div class="rounded-xl border border-border p-3"><div class="text-[10px] uppercase tracking-wider text-muted-foreground">Texto alternativo</div><div class="mt-1 text-sm">' + escapeAttribute(item.alt || '—') + '</div></div>' +
                    '</div>' +
                '</div>';
            $('#gallery-detail-content').html(html);
            openModal('#gallery-detail-modal');
        }

        function renderFormPreview($form, media, type, poster) {
            const $box = $form.find('.gallery-preview-box');
            $box.find('.gallery-generated-preview').remove();
            $box.find('.gallery-preview-placeholder').toggle(!media);
            if (!media) return;
            $box.append('<div class="gallery-generated-preview absolute inset-0">' + mediaMarkup({media: media, type: type, poster: poster, alt: 'Vista previa'}, false) + '</div>');
        }

        function syncSourceInterface($form) {
            const source = $form.find('.js-gallery-source').val() || 'ARCHIVO';
            const type = $form.find('.js-gallery-type').val() || 'IMAGEN';
            $form.find('.gallery-file-source').toggleClass('hidden', source !== 'ARCHIVO');
            $form.find('.gallery-url-source').toggleClass('hidden', source !== 'URL');
            $form.find('.gallery-file-poster').toggleClass('hidden', source !== 'ARCHIVO' || type !== 'VIDEO');
            $form.find('.gallery-url-poster').toggleClass('hidden', source !== 'URL' || type !== 'VIDEO');
            $form.find('.js-gallery-media-url').prop('required', source === 'URL');
            $form.find('.gallery-source-btn').each(function () {
                const active = $(this).data('source-mode') === source;
                $(this).toggleClass('bg-background text-foreground shadow-sm', active).toggleClass('text-muted-foreground', !active);
            });
        }

        function showSelectedFile($form, fileName, removable) {
            const $row = $form.find('.gallery-selected-file');
            $row.removeClass('hidden').addClass('flex');
            $row.find('.gallery-selected-file-name').text(fileName);
            $row.find('.js-gallery-remove-file').toggle(Boolean(removable));
        }

        function clearSelectedFile($form) {
            $form.find('.js-gallery-media-file').val('');
            $form.find('.gallery-selected-file').addClass('hidden').removeClass('flex');
            const current = $form.data('current-media') || '';
            renderFormPreview($form, current, $form.data('current-type') || $form.find('.js-gallery-type').val(), $form.data('current-poster') || '');
        }

        function resetNewForm() {
            const form = document.getElementById('gallery-new-form');
            form.reset();
            const $form = $(form);
            $form.removeData('current-media current-poster current-type');
            $form.find('.js-gallery-source').val('ARCHIVO');
            $form.find('.js-gallery-type').val('IMAGEN');
            $form.find('[name="diseno"]').val('ESTANDAR');
            $form.find('[name="publicado"]').val('true');
            $form.find('input[type="checkbox"][name="destacado"]').prop('checked', false);
            $form.find('.gallery-order-display').val('Automático');
            $form.find('.gallery-selected-file').addClass('hidden').removeClass('flex');
            if (!$form.find('[name="fechaEvento"]').val()) $form.find('[name="fechaEvento"]').val(new Date().toISOString().slice(0, 10));
            syncSourceInterface($form);
            renderFormPreview($form, '', 'IMAGEN', '');
        }

        function fillEdit(item) {
            const $form = $('#gallery-edit-form');
            $form.find('[name="id"]').val(item.id);
            $form.find('.js-gallery-source').val(item.source);
            $form.find('.js-gallery-type').val(item.type);
            $form.find('[name="mediaUrl"]').val(item.source === 'URL' ? item.media : '');
            $form.find('[name="portadaUrl"]').val(/^https?:\/\//i.test(item.poster) ? item.poster : '');
            $form.find('[name="titulo"]').val(item.title);
            $form.find('[name="categoriaId"]').val(item.categoryId);
            $form.find('[name="descripcion"]').val(item.description);
            $form.find('[name="textoAlternativo"]').val(item.alt);
            $form.find('[name="diseno"]').val(item.design);
            $form.find('[name="publicado"]').val(String(item.published));
            $form.find('input[type="checkbox"][name="destacado"]').prop('checked', item.featured);
            $form.find('[name="fechaEvento"]').val(item.date);
            $form.find('.gallery-order-display').val('#' + item.order);
            $form.find('.js-gallery-media-file, .js-gallery-poster-file').val('');
            $form.data('current-media', item.media).data('current-poster', item.poster).data('current-type', item.type);
            if (item.source === 'ARCHIVO') showSelectedFile($form, 'Archivo actual · carga otro para reemplazarlo', false);
            else $form.find('.gallery-selected-file').addClass('hidden').removeClass('flex');
            syncSourceInterface($form);
            renderFormPreview($form, item.media, item.type, item.poster);
            openModal('#gallery-edit-modal');
        }

        function validateMainFile($input) {
            const file = $input[0].files[0];
            if (!file) return true;
            const $form = $input.closest('form');
            const type = $form.find('.js-gallery-type').val();
            const expectsVideo = type === 'VIDEO';
            const isVideo = file.type.startsWith('video/');
            const limit = expectsVideo ? 100 * 1024 * 1024 : 10 * 1024 * 1024;
            if (expectsVideo !== isVideo) {
                Swal.fire({icon: 'warning', title: 'Tipo de archivo incorrecto', text: expectsVideo ? 'Selecciona un video MP4 o WebM.' : 'Selecciona una imagen JPG, PNG o WebP.'});
                $input.val(''); return false;
            }
            if (file.size > limit) {
                Swal.fire({icon: 'warning', title: 'Archivo demasiado grande', text: expectsVideo ? 'El video no puede superar 100 MB.' : 'La imagen no puede superar 10 MB.'});
                $input.val(''); return false;
            }
            showSelectedFile($form, file.name, true);
            renderFormPreview($form, URL.createObjectURL(file), type, '');
            return true;
        }

        function setView(view) {
            const cards = view !== 'table';
            $('#gallery-cards').toggleClass('hidden', !cards);
            $('#gallery-table').toggleClass('hidden', cards);
            $('#gallery-view-cards').toggleClass('bg-background text-foreground shadow-sm', cards).toggleClass('text-muted-foreground', !cards);
            $('#gallery-view-table').toggleClass('bg-background text-foreground shadow-sm', !cards).toggleClass('text-muted-foreground', cards);
            localStorage.setItem(VIEW_KEY, cards ? 'cards' : 'table');
        }

        $('#btn-new-gallery').on('click', function () { resetNewForm(); openModal('#gallery-new-modal'); });
        $('#btn-gallery-categories').on('click', function () { openModal('#gallery-categories-modal'); });
        $('.js-close-gallery-modal').on('click', closeModals);
        $(document).on('keydown', function (event) { if (event.key === 'Escape') closeModals(); });

        $(document).on('click', '.js-gallery-preview', function () { showDetail(recordFrom($(this))); });
        $(document).on('click', '.js-gallery-action', function () {
            const item = recordFrom($(this));
            $('.gallery-card-menu').addClass('hidden');
            if ($(this).data('gallery-action') === 'edit') fillEdit(item); else showDetail(item);
        });
        $('#gallery-detail-edit').on('click', function () { if (detailItem) fillEdit(detailItem); });

        $('.js-gallery-menu-trigger').on('click', function (event) {
            event.stopPropagation();
            const $menu = $(this).next('.gallery-card-menu');
            $('.gallery-card-menu').not($menu).addClass('hidden');
            $menu.toggleClass('hidden');
        });
        $(document).on('click', function (event) { if (!$(event.target).closest('.gallery-card-menu, .js-gallery-menu-trigger').length) $('.gallery-card-menu').addClass('hidden'); });

        $('.gallery-source-btn').on('click', function () {
            const $form = $(this).closest('form');
            $form.find('.js-gallery-source').val($(this).data('source-mode')).trigger('change');
        });
        $('.js-gallery-source, .js-gallery-type').on('change', function () {
            const $form = $(this).closest('form');
            syncSourceInterface($form);
            const current = $form.data('current-media') || '';
            renderFormPreview($form, current, $form.find('.js-gallery-type').val(), $form.data('current-poster') || '');
        });

        $('.js-gallery-media-file').on('change', function () { validateMainFile($(this)); });
        $('.js-gallery-remove-file').on('click', function () { clearSelectedFile($(this).closest('form')); });
        $('.js-gallery-media-url').on('input', function () {
            const $form = $(this).closest('form'); const url = $(this).val().trim();
            if (/^https?:\/\//i.test(url)) renderFormPreview($form, url, $form.find('.js-gallery-type').val(), $form.find('[name="portadaUrl"]').val());
        });

        $('.gallery-dropzone').on('dragenter dragover', function (event) { event.preventDefault(); $(this).addClass('is-dragging'); });
        $('.gallery-dropzone').on('dragleave drop', function (event) { event.preventDefault(); $(this).removeClass('is-dragging'); });
        $('.gallery-dropzone').on('drop', function (event) {
            const files = event.originalEvent.dataTransfer.files;
            if (!files.length) return;
            const input = $(this).find('.js-gallery-media-file')[0];
            const transfer = new DataTransfer(); transfer.items.add(files[0]); input.files = transfer.files;
            $(input).trigger('change');
        });

        $('#gallery-new-form, #gallery-edit-form').on('submit', function (event) {
            const $form = $(this); const creating = this.id === 'gallery-new-form';
            const source = $form.find('.js-gallery-source').val();
            const hasFile = Boolean($form.find('.js-gallery-media-file')[0].files.length);
            const changedType = !creating && $form.data('current-type') && $form.data('current-type') !== $form.find('.js-gallery-type').val();
            if (source === 'ARCHIVO' && ((creating && !hasFile) || (changedType && !hasFile))) {
                event.preventDefault();
                Swal.fire({icon: 'warning', title: 'Formulario incompleto', text: changedType ? 'Al cambiar el tipo debes seleccionar un archivo compatible.' : 'Selecciona el archivo principal.'});
            }
        });

        $('.js-gallery-delete-form').on('submit', function (event) {
            event.preventDefault();
            const id = $(this).find('[name="id"]').val();
            const title = $(this).find('[name="titulo"]').val();

            $('#gallery-delete-id').val(id);
            $('#gallery-delete-name').text('“' + title + '”');
            $('#gallery-delete-confirmation').val('');
            $('#gallery-delete-submit').prop('disabled', true);
            $('#gallery-delete-help').removeClass('text-destructive').addClass('text-muted-foreground');
            $('.gallery-card-menu').addClass('hidden');

            openModal('#gallery-delete-modal');
            setTimeout(function () { $('#gallery-delete-confirmation').trigger('focus'); }, 100);
        });

        $('#gallery-delete-confirmation').on('input', function () {
            const valid = $(this).val().trim().toUpperCase() === 'ELIMINAR';
            $('#gallery-delete-submit').prop('disabled', !valid);
            $('#gallery-delete-help')
                .toggleClass('text-destructive', $(this).val().length > 0 && !valid)
                .toggleClass('text-muted-foreground', $(this).val().length === 0 || valid)
                .text(valid
                    ? 'Confirmación correcta. Ya puedes eliminar el archivo.'
                    : 'Escribe exactamente la palabra ELIMINAR.');
        });

        $('#gallery-delete-confirm-form').on('submit', function (event) {
            if ($('#gallery-delete-confirmation').val().trim().toUpperCase() !== 'ELIMINAR') {
                event.preventDefault();
                $('#gallery-delete-confirmation').trigger('focus');
            }
        });
        $('.js-category-delete-form').on('submit', function (event) {
            if ($(this).data('confirmed')) return;
            event.preventDefault(); const form = this; const name = $(this).find('[name="nombre"]').val();
            Swal.fire({title: '¿Eliminar categoría?', text: 'Se eliminará “' + name + '”.', icon: 'warning', showCancelButton: true, confirmButtonText: 'Eliminar', cancelButtonText: 'Cancelar', confirmButtonColor: '#dc2626'}).then(function (result) { if (result.isConfirmed) { $(form).data('confirmed', true); form.submit(); } });
        });

        $('#gallery-search').on('input', function () { clearTimeout(searchTimer); searchTimer = setTimeout(function () { $('#gallery-filter-form').trigger('submit'); }, 450); });
        $('.gallery-auto-filter').on('change', function () { $('#gallery-filter-form').trigger('submit'); });
        $('#gallery-view-cards').on('click', function () { setView('cards'); });
        $('#gallery-view-table').on('click', function () { setView('table'); });

        setView(localStorage.getItem(VIEW_KEY) || 'cards');
        $('#gallery-new-form, #gallery-edit-form').each(function () { syncSourceInterface($(this)); });

        const modal = String($page.data('open-modal') || '');
        if (modal === 'nuevo') openModal('#gallery-new-modal');
        if (modal === 'editar') openModal('#gallery-edit-modal');
        if (String($page.data('open-categories')) === 'true') openModal('#gallery-categories-modal');

        const success = $page.data('success-message'); const error = $page.data('error-message');
        if (success) Swal.fire({icon: 'success', title: 'Listo', text: success, timer: 3200, showConfirmButton: false});
        if (error) Swal.fire({icon: 'error', title: 'No fue posible completar la acción', text: error});
    });
})(jQuery);