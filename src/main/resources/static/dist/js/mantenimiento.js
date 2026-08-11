(function ($) {
    'use strict';

    $(function () {
        const $page = $('#page-content');
        let searchTimer = null;

        function refreshIcons() {
            if (window.lucide) window.lucide.createIcons();
        }

        function openModal(selector) {
            $('.maintenance-modal').addClass('hidden').removeClass('flex');
            $(selector).removeClass('hidden').addClass('flex');
            $('body').addClass('overflow-hidden');
            refreshIcons();
        }

        function closeModals() {
            $('.maintenance-modal').addClass('hidden').removeClass('flex');
            $('body').removeClass('overflow-hidden');
        }

        function recordFromRow($row) {
            return {
                id: String($row.attr('data-order-id') || ''),
                number: $row.attr('data-order-number') || 'Orden',
                equipmentId: String($row.attr('data-equipment-id') || ''),
                clientId: String($row.attr('data-client-id') || ''),
                technicianId: String($row.attr('data-technician-id') || ''),
                type: $row.attr('data-type') || '',
                priority: $row.attr('data-priority') || '',
                state: $row.attr('data-state') || '',
                location: $row.attr('data-location') || '',
                date: $row.attr('data-date') || '',
                time: ($row.attr('data-time') || '').substring(0, 5),
                cost: $row.attr('data-cost') || '0',
                duration: $row.attr('data-duration') || '2',
                description: $row.attr('data-description') || ''
            };
        }

        function findRecord(id) {
            let found = null;
            $('.maintenance-record').each(function () {
                const record = recordFromRow($(this));
                if (record.id === String(id)) {
                    found = record;
                    return false;
                }
            });
            return found;
        }

        function showDetail(record) {
            const template = document.getElementById('maintenance-detail-template-' + record.id);
            if (!template) return;
            $('#maintenance-detail-title').text('Detalle de ' + record.number);
            $('#maintenance-detail-content').html(template.innerHTML);
            openModal('#modal-maintenance-detail');
        }

        function fillEdit(record, resetValues) {
            if (resetValues) {
                $('#edit-order-id').val(record.id);
                $('#edit-order-equipment').val(record.equipmentId);
                $('#edit-order-client').val(record.clientId);
                $('#edit-order-technician').val(record.technicianId);
                $('#edit-order-type').val(record.type);
                $('#edit-order-priority').val(record.priority);
                $('#edit-order-state').val(record.state);
                $('#edit-order-location').val(record.location);
                $('#edit-order-date').val(record.date);
                $('#edit-order-time').val(record.time);
                $('#edit-order-cost').val(record.cost);
                $('#edit-order-duration').val(record.duration);
                $('#edit-order-description').val(record.description);
            }
            $('#edit-maintenance-title').text('Editar ' + record.number);
            openModal('#modal-edit-maintenance');
        }

        function fillTechnician(record, resetValues) {
            $('#assign-order-id').val(record.id);
            $('#assign-technician-summary').text(record.number + ' · Selecciona un responsable disponible.');
            if (resetValues) {
                $('#form-assign-technician input[name="tecnicoId"]').prop('checked', false)
                    .filter('[value="' + record.technicianId + '"]').prop('checked', true);
            }
            openModal('#modal-assign-technician');
        }

        function fillObservation(record, resetValues) {
            $('#observation-order-id').val(record.id);
            $('#observation-order-summary').text(record.number + ' · Registra un hallazgo o acción tomada.');
            if (resetValues) $('#form-add-observation textarea[name="texto"]').val('');
            openModal('#modal-add-observation');
        }

        function fillEvidence(record) {
            $('#evidence-order-id').val(record.id);
            $('#evidence-order-summary').text(record.number + ' · Adjunta imágenes o documentos del trabajo.');
            openModal('#modal-upload-evidence');
        }

        function fillClose(record, resetValues) {
            $('#close-order-id').val(record.id);
            $('#close-order-summary').text(record.number + ' · El checklist debe estar completo.');
            if (resetValues) {
                $('#form-close-maintenance textarea[name="resumen"]').val('');
                $('#form-close-maintenance input[name="horasReales"]').val(record.duration || '2');
                $('#form-close-maintenance input[name="proximaRevision"]').val('');
            }
            openModal('#modal-close-maintenance');
        }

        function fillDelete(record, resetValues) {
            $('#delete-order-id').val(record.id);
            $('#delete-order-summary').text(record.number);
            if (resetValues) $('#form-delete-maintenance input[name="confirmacion"]').val('');
            openModal('#modal-delete-maintenance');
        }

        function handleAction(action, record, resetValues) {
            if (action === 'detail') showDetail(record);
            if (action === 'edit') fillEdit(record, resetValues);
            if (action === 'technician') fillTechnician(record, resetValues);
            if (action === 'observation') fillObservation(record, resetValues);
            if (action === 'evidence') fillEvidence(record);
            if (action === 'close') fillClose(record, resetValues);
            if (action === 'delete') fillDelete(record, resetValues);
        }

        function markInvalid($control, invalid) {
            $control.toggleClass('border-destructive ring-1 ring-destructive/30', invalid);
        }

        function validateForm(form) {
            let firstInvalid = null;
            $(form).find('.maintenance-control').each(function () {
                const invalid = !this.checkValidity();
                markInvalid($(this), invalid);
                if (invalid && !firstInvalid) firstInvalid = this;
            });
            if (firstInvalid) {
                firstInvalid.reportValidity();
                firstInvalid.focus();
                return false;
            }
            return true;
        }

        $('#btn-new-maintenance').on('click', function () {
            openModal('#modal-new-maintenance');
        });

        $('.js-maintenance-close-modal').on('click', closeModals);
        $('.maintenance-modal').on('mousedown', function (event) {
            if (event.target === this) closeModals();
        });
        $(document).on('keydown', function (event) {
            if (event.key === 'Escape') closeModals();
        });

        $('.maintenance-record').on('click', function (event) {
            if ($(event.target).closest('.maintenance-actions').length) return;
            showDetail(recordFromRow($(this)));
        });

        $('.js-maintenance-action').on('click', function (event) {
            event.stopPropagation();
            handleAction(
                $(this).attr('data-maintenance-action'),
                recordFromRow($(this).closest('.maintenance-record')),
                true
            );
        });

        $(document).on('change', '.js-checklist-toggle', function () {
            $(this).closest('form').trigger('submit');
        });

        $('.maintenance-control').on('input change', function () {
            markInvalid($(this), !this.checkValidity());
        });
        $('#form-new-maintenance, #form-edit-maintenance, #form-assign-technician, #form-add-observation, #form-close-maintenance').on('submit', function (event) {
            if (!validateForm(this)) event.preventDefault();
        });
        $('#form-delete-maintenance').on('submit', function (event) {
            const $confirmation = $(this).find('[name="confirmacion"]');
            if ($confirmation.val().trim().toUpperCase() !== 'ELIMINAR') {
                event.preventDefault();
                $confirmation[0].setCustomValidity('Escribe ELIMINAR para confirmar.');
            } else {
                $confirmation[0].setCustomValidity('');
            }
            if (!validateForm(this)) event.preventDefault();
        });
        $('#form-delete-maintenance [name="confirmacion"]').on('input', function () {
            this.setCustomValidity('');
        });

        $('#maintenance-evidence-files').on('change', function () {
            const files = Array.from(this.files || []);
            const $list = $('#maintenance-selected-files');
            if (!files.length) {
                $list.addClass('hidden').empty();
                return;
            }
            const invalid = files.length > 8 || files.some(function (file) {
                return file.size > 20 * 1024 * 1024 || !/\.(png|jpe?g|pdf)$/i.test(file.name);
            });
            if (invalid) {
                this.value = '';
                $list.addClass('hidden').empty();
                if (window.Swal) window.Swal.fire({icon: 'error', title: 'Archivos no permitidos', text: 'Selecciona hasta 8 archivos JPG, PNG o PDF de máximo 20 MB cada uno.'});
                return;
            }
            $list.removeClass('hidden').html(files.map(function (file) {
                return '<div class="flex items-center gap-2 rounded-md border border-border p-2 text-xs"><span class="min-w-0 flex-1 truncate"></span><span class="text-muted-foreground">' + (file.size / 1024 / 1024).toFixed(2) + ' MB</span></div>';
            }).join(''));
            $list.children().each(function (index) {
                $(this).find('span').first().text(files[index].name);
            });
        });
        $('#form-upload-evidence').on('submit', function (event) {
            if (!$('#maintenance-evidence-files')[0].files.length) {
                event.preventDefault();
                $('#maintenance-evidence-files')[0].setCustomValidity('Selecciona al menos un archivo.');
                $('#maintenance-evidence-files')[0].reportValidity();
            }
        });

        $('#btn-toggle-maintenance-filters').on('click', function () {
            $('#maintenance-filter-options').toggleClass('hidden flex');
        });
        $('#maintenance-type-filter, #maintenance-state-filter, #maintenance-priority-filter').on('change', function () {
            $('#maintenance-filter-form').trigger('submit');
        });
        $('#maintenance-search').on('input', function () {
            window.clearTimeout(searchTimer);
            searchTimer = window.setTimeout(function () {
                $('#maintenance-filter-form').trigger('submit');
            }, 450);
        });

        function showFeedback() {
            if (!window.Swal) return;
            const success = $page.attr('data-success-message');
            const error = $page.attr('data-error-message');
            if (error) {
                window.Swal.fire({icon: 'error', title: 'No fue posible completar la acción', text: error, confirmButtonColor: '#5eead4'});
            } else if (success) {
                window.Swal.fire({icon: 'success', title: success, confirmButtonColor: '#5eead4'});
            }
        }

        const detailId = $page.attr('data-open-detail-id');
        const modal = $page.attr('data-open-modal');
        const modalOrderId = $page.attr('data-order-modal-id');
        if (detailId) {
            const record = findRecord(detailId);
            if (record) showDetail(record);
        } else if (modal === 'nueva') {
            openModal('#modal-new-maintenance');
        } else if (modal && modalOrderId) {
            const record = findRecord(modalOrderId);
            if (record) handleAction(modal, record, false);
        }

        showFeedback();
        refreshIcons();
    });
})(jQuery);
