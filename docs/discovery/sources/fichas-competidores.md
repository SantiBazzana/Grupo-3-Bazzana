# Fichas de competidores — relevamiento de fuentes

**Fecha de consulta de todas las fuentes**: 2026-11-20
**Método**: lectura de las páginas públicas del dominio oficial de cada proveedor (máximo 4–5 páginas por producto: inicio, precios, funcionalidades, ayuda o privacidad). No se crearon cuentas ni se contactó a proveedores.

**Convenciones**
- **[COMPROBADO]**: la funcionalidad se ve descripta en concreto, en una guía, una demo, una captura o la documentación.
- **[COMERCIAL]**: afirmación del proveedor sin demostración (cifras de clientes, "el mejor", certificaciones sin documento).
- **No evidenciado**: el dato no figura en las páginas leídas. No significa que no exista.

**Descartados por falta de evidencia legible**
- **Doctoralia (Docplanner) Argentina**: `pro.doctoralia.com.ar` no resolvió por DNS y `www.doctoralia.com.ar` rechazó la conexión (dos intentos, 2026-11-20). Se cubre parcialmente a través de Clinic Cloud, que es de Doctoralia España SL.
- **Reservo (reservo.cl)**: el sitio se renderiza con JavaScript y solo se obtuvieron 363 caracteres de la portada. No hay evidencia suficiente para puntuarlo.

---

## Argentina

### 1. Bilog
- **Empresa / país**: Bilog, empresa argentina, "más de 20 años" [COMERCIAL] · https://bilog.com.ar/
- **Segmento**: consultorios, clínicas, auditoría de obras sociales y gerenciadoras, facultades.
- **Modalidad**: web (app.bilog.com.ar), app iOS/Android y escritorio.
- **Agenda**: [COMPROBADO] vista diaria, semanal o por profesional; mover turnos arrastrándolos y estirarlos para cambiar la duración; sobreturno desde la app; múltiples profesionales y consultorios. Sillones/boxes, bloqueos y prevención de solapamientos: No evidenciado.
- **Turnos digitales**: [COMPROBADO] agenda online para reservar, cancelar y consultar, con acceso por QR o link. Chatbot "iAngela" para sacar turno [COMERCIAL]. Lista de espera: No evidenciado.
- **Automatización**: [COMPROBADO] recordatorios por WhatsApp y SMS; email [COMERCIAL, solo en llms.txt]; estado de presentismo y "ausente con aviso". Campañas y recuperación de pacientes: No evidenciado.
- **Clínica**: ficha, odontograma, tratamientos, adjuntos, presupuestos y consentimientos con firma por QR. Anamnesis y periodontograma: No evidenciado.
- **Administración**: caja, liquidación de obras sociales, pagos y reportes. Mercado Pago y ARCA: No evidenciado.
- **Integraciones**: firma electrónica (Ley 25.506). El propio sitio declara "No hay una API pública documentada".
- **Operación**: sucursales, roles y permisos, soporte telefónico local.
- **Seguridad**: roles. Leyes 25.326 y 26.529 y backup: No evidenciado.
- **Comercial**: precios no publicados; mensual, sin permanencia, configuración inicial sin costo [COMERCIAL].
- **Adopción**: "+1.500 clínicas", "+5.000 usuarios", "+150.000 turnos/mes" [COMERCIAL].
- **Fuentes**: https://bilog.com.ar/ · https://bilog.com.ar/llms.txt · https://bilog.com.ar/agenda

### 2. Dentalink (sitio Argentina)
- **Empresa / país**: Engenis SpA, Santiago de Chile · https://www.softwaredentalink.com/ar
- **Segmento**: consultas individuales y clínicas con varios profesionales y sucursales; módulo de laboratorio.
- **Modalidad**: nube, desde PC, tablet o celular.
- **Agenda**: "control de agenda", bloques horarios limitables, usuarios ilimitados con roles [COMERCIAL]. Vistas, sillones, sobreturnos y solapamientos: No evidenciado.
- **Turnos digitales**: agendamiento online 24/7 con link único para WhatsApp, Instagram y web; captura de la pantalla de reserva [COMPROBADO]. "Contact Center IA" para WhatsApp: agenda, confirma, reprograma y cancela; costo extra desde USD 50/mes [COMERCIAL].
- **Automatización**: recordatorios y confirmaciones por WhatsApp. Reagendamiento automático, email marketing y NPS solo en el plan Titanium [COMERCIAL].
- **Clínica**: historia clínica, odontograma, periodontograma, presupuestos y consentimientos. La maqueta de odontograma está visible [COMPROBADO, maqueta].
- **Administración**: caja, cobros y obras sociales; gastos, inventario y comisiones en Pro. Facturación ARCA y Mercado Pago: No evidenciado.
- **Integraciones**: API, receta electrónica y firma, todas como adicionales pagos.
- **Operación**: multicentro (Pro), roles, exportación de datos, centro de ayuda con tutoriales.
- **Seguridad**: ISO 27001, AWS cifrado, backups diarios, HIPAA [COMERCIAL, sin certificado visible].
- **Comercial**: precios por cotización; mensual, semestral o anual, sin permanencia; "demo gratis".
- **Adopción**: "+15.000 clientes", "+20 países" [COMERCIAL].
- **Fuentes**: https://www.softwaredentalink.com/ar · /ar/planes · /ar/soporte · /ar/atrae-mas-pacientes/agendamiento-online

### 3. Oclusar
- **Empresa / país**: Argentina (precios en pesos y leyes argentinas citadas); razón social no evidenciada · https://www.oclusar.com.ar/
- **Segmento**: consultorios individuales y clínicas con sillones; alquiler de sillón.
- **Modalidad**: web y celular, sin instalación.
- **Agenda**: [COMPROBADO] vista de día y semana con color por estado; horarios y bloqueos por profesional; sobreturno explícito; **la base de datos impide el doble turno del mismo profesional**; múltiples profesionales y sillones. Duración variable: No evidenciado.
- **Turnos digitales**: [COMPROBADO] link de agendamiento donde el paciente elige un horario y el profesional lo aprueba. Seña por Mercado Pago [COMERCIAL, solo maqueta]. Cancelación, reprogramación y lista de espera: No evidenciado.
- **Automatización**: [COMPROBADO] recordatorios por WhatsApp con click-to-chat que el usuario dispara a mano ("Los recordatorios no salen solos").
- **Clínica**: historia clínica con evolución y odontograma, firmada y sin edición posterior; consentimientos, recetas y presupuestos [COMERCIAL].
- **Administración**: cobros (Mercado Pago, efectivo, transferencia), factura electrónica ARCA y presentaciones a obras sociales [COMERCIAL].
- **Integraciones**: WhatsApp click-to-chat, Google Calendar, Instagram, Mercado Pago, ARCA.
- **Operación**: roles, asignación de sillones, auditoría de ediciones y exportación completa [COMERCIAL].
- **Seguridad**: cifrado y copias diarias [COMERCIAL]; declara las Leyes 26.529, 25.326 y 25.506, registro en el Ministerio de Salud y AAIP "en trámite" [COMERCIAL].
- **Comercial**: "Empezás gratis, sin tarjeta"; montos de los planes: No evidenciado.
- **Adopción**: No evidenciado.
- **Fuentes**: https://www.oclusar.com.ar/ · /guias · /guias/agenda-y-recordatorios-por-whatsapp

### 4. OdontoGRAMA
- **Empresa / país**: pie de página "© 2023 Julio Santolo", logo enlazado a solsoftware.com.ar; país: No evidenciado (dominio .com.ar) · https://odontograma.com.ar/
- **Segmento**: consultorios con asistentes y profesionales asociados.
- **Modalidad**: nube, "desde su PC, tablet o celular" [COMERCIAL].
- **Agenda**: "agendas de turnos personalizables con control de asistencia" [COMERCIAL]. El resto: No evidenciado.
- **Turnos digitales / automatización**: No evidenciado.
- **Clínica**: ficha con imágenes, tratamientos y diagnósticos [COMERCIAL].
- **Administración**: caja, cuentas corrientes, facturación a prepagas y obras sociales, gastos y estadísticas [COMERCIAL].
- **Comercial**: "Acceda a todas las funciones del sistema ¡GRATIS!" [COMERCIAL]; sin precios publicados.
- **Fuentes**: https://odontograma.com.ar/ (página única)

### 5. Odonto.ar
- **Empresa / país**: Córdoba, Argentina · https://odonto.ar/
- **Segmento**: profesional individual, consultorio con secretaria o varios profesionales, clínica de hasta 10 personas.
- **Modalidad**: web en la nube; datos en Supabase.
- **Agenda**: vista por día, semana o mes [COMERCIAL]; [COMPROBADO] **duración por motivo de consulta (30 o 60 min)** en la demo. Boxes, bloqueos y solapamientos: No evidenciado.
- **Turnos digitales**: [COMPROBADO] link público de reserva con motivo, día y horario (demo interactiva). Cancelación y reprogramación "con reglas de la clínica" [COMERCIAL].
- **Automatización**: No evidenciado.
- **Clínica**: odontograma con estados realizado, planificado y a rehacer [COMPROBADO, maqueta]; historia clínica, radiografías y presupuestos [COMERCIAL].
- **Administración**: panel de finanzas y cálculo de cobertura de obra social [COMERCIAL]. ARCA: No evidenciado.
- **Seguridad**: declara Ley 25.326 y AAIP, TLS, RLS y roles; advierte que los datos pueden procesarse fuera de Argentina.
- **Comercial**: prueba de 30 días sin tarjeta; USD 29 / 49 / 99 por mes (aprox. ARS 45.240 / 76.440 / 154.440), cobrados en pesos por Mercado Pago.
- **Adopción**: No evidenciado.
- **Fuentes**: https://odonto.ar/ · /demo · /privacidad

### 6. Dentia
- **Empresa / país**: "Hecho en Argentina" · https://dentia.tech/
- **Segmento**: consultorios (plan Básico) y clínicas (Pro y Elite).
- **Modalidad**: navegador; se instala como app web (PWA).
- **Agenda**: [COMPROBADO] calendario por profesional, sobreturnos y sala de espera. Boxes, bloqueos y solapamientos: No evidenciado.
- **Turnos digitales**: [COMPROBADO] asistente de IA en WhatsApp que consulta disponibilidad, agenda, reprograma y cancela, con demo en la portada; confirmación por WhatsApp; bandeja unificada con Instagram y Messenger.
- **Automatización**: recordatorios por email (Básico) y WhatsApp (Pro); cumpleaños, reseñas y cuotas; recaptación y campañas de controles (Elite).
- **Clínica**: historia configurable, odontograma, periodontograma por voz, planes, presupuestos, consentimientos, laboratorio; cefalometría y hallazgos en panorámica con IA.
- **Administración**: [COMPROBADO] caja, cuotas, cuentas corrientes y **facturación ARCA A/B/C**; Mercado Pago; liquidaciones de obras sociales y prepagas; seña al reservar (Elite).
- **Integraciones**: WhatsApp, Google Calendar, ARCA, Mercado Pago, Meta; firma con hash SHA-256 y QR.
- **Operación**: sucursales (1, 3 o ilimitadas), 17 perfiles de rol, auditoría, importación y exportación.
- **Seguridad**: declara Ley 25.326 y verificación en dos pasos. Backup: No evidenciado.
- **Comercial**: 30 días gratis; ARS 20.000 / 30.000 / 40.000 por mes.
- **Adopción**: "+100 clínicas" [COMERCIAL].
- **Fuentes**: https://dentia.tech/ (página única con secciones #precios, #bot, #faq)

### 7. Órbita (hiorbita)
- **Empresa / país**: Órbita Global, La Plata, Argentina · https://hiorbita.com/
- **Segmento**: consultorios y clínicas odontológicas; plan Red para 4 o más sedes.
- **Modalidad**: SaaS en navegador; app de chat en iOS; AWS.
- **Agenda**: [COMERCIAL, producto en preventa al 2026-11-26] **agenda por sillón que impide el choque y ofrece horarios libres**; tutorial de bloqueo de horarios (solo título). Vistas y duración: No evidenciado.
- **Turnos digitales**: [COMPROBADO] chat de IA en WhatsApp (API oficial de Meta) que atiende 24 h, da, reagenda y cancela turnos, con confirmación por botones.
- **Automatización**: recordatorios a 48 y 24 h; recontacto de quien consultó y no volvió; pedido de reseña.
- **Clínica**: odontograma por voz [COMPROBADO], periodontograma, historia clínica con registro de correcciones, presupuestos.
- **Administración**: caja por sede con Mercado Pago, factura ARCA, convenios y liquidaciones de obras sociales.
- **Integraciones**: API de WhatsApp, ARCA, Dentalink y Benty.
- **Operación**: multisede, roles configurables, log de accesos, exportación total, 45 tutoriales.
- **Seguridad**: verificación en dos pasos y registro de apertura de fichas. Normativa: No evidenciado.
- **Comercial**: precios a cotizar, en USD por sede; Meta cobra USD 0,19 por paciente aparte.
- **Adopción**: 12 logos de clínicas y 4 reseñas; "1.219 turnos en 4 semanas" [COMERCIAL].
- **Fuentes**: https://hiorbita.com/ · /precios · /erp · /consultorios

### 8. OdontoClinIA (ClinIA)
- **Empresa / país**: Emprinet, "software argentino" · https://www.clinia.com.ar/odontologia
- **Segmento**: consultorios, clínicas PyME, municipios y hospitales, cadenas.
- **Modalidad**: web con instancia y subdominio por cliente; app mencionada.
- **Agenda**: turnos por profesional, especialidad o sillón. El resto: No evidenciado.
- **Turnos digitales**: [COMPROBADO, descripto] turnero web y app; IA en WhatsApp que valida contra la agenda, confirma, cancela y reprograma.
- **Automatización**: [COMPROBADO, descripto] recordatorios por WhatsApp y correo.
- **Clínica**: [COMPROBADO] odontograma FDI de 52 piezas con versiones (demo interactiva); historia clínica con CIE-10; plan de tratamiento valorizado según nomenclador.
- **Administración**: caja multimoneda, facturación ARCA, **verificación de cobertura por PUCO**.
- **Integraciones**: WhatsApp, ARCA, PUCO, RENAPER; HL7 FHIR para recetas.
- **Seguridad**: declara Ley 25.326, backups cada 10 minutos; "No afirmamos certificaciones".
- **Comercial**: cotización por institución; demo de 45 minutos.
- **Adopción**: 15 municipios facturan PAMI (dato general, no odontológico).
- **Fuentes**: https://www.clinia.com.ar/odontologia · /faq · /seguridad · /catalogo-funcional.pdf (sin texto extraíble)

---

## Latinoamérica

### 9. AgendaPro
- **Empresa / país**: AgendaPro Inc.; país de origen: No evidenciado · https://agendapro.com/ar/dental/software-odontologico
- **Segmento**: clínicas y consultorios de salud y estética; landing odontológica para Argentina.
- **Modalidad**: nube y apps iOS/Android.
- **Agenda**: [COMPROBADO] **por odontólogo, práctica y sillón, con duración por práctica**. Bloqueos y solapamientos: No evidenciado.
- **Turnos digitales**: [COMPROBADO] reserva desde web propia, Google Maps y redes; agente IA en WhatsApp e Instagram; la "Recepcionista IA" reagenda, cancela y cobra señas.
- **Automatización**: [COMPROBADO] recordatorios por WhatsApp; reactivación de inasistencias y campañas (Pro IA).
- **Clínica**: [COMPROBADO] historia con anamnesis, odontograma, evoluciones, radiografías, presupuestos y consentimiento firmado.
- **Administración**: [COMPROBADO] caja, **seña o pago con Mercado Pago**, cuotas, QR y link de pago. Obras sociales: No evidenciado. Factura electrónica "Próximamente".
- **Integraciones**: Mercado Pago, Google Calendar, WhatsApp; API pública a USD 200/mes.
- **Seguridad**: cita las Leyes 25.326 y 26.529, cifrado, AWS (texto leído de forma parcial).
- **Comercial**: prueba de 14 días; ARS 9.900 / 19.900 / 39.900 / 129.900 por mes; extras por profesional. Al 2026-11-26 los precios aclaran "IVA incluido" y la portada ya no dice "desde $13.900" (la inconsistencia del 2026-11-20 desapareció).
- **Adopción**: "+30.000 clínicas en LATAM", 4,8 con "+1.200 reseñas" [COMERCIAL].
- **Fuentes**: https://agendapro.com/ar · /ar/planes · /ar/dental/software-odontologico

### 10. Dentidesk
- **Empresa / país**: Providencia, Santiago, Chile · https://www.dentidesk.com/
- **Agenda**: [COMPROBADO] horarios configurables con intervalos de 15, 30 o 40 min y feriados que bloquean la agenda.
- **Turnos digitales**: [COMPROBADO] botón de agendamiento online para la web; confirmación o anulación por correo el día anterior. WhatsApp automático figura como "próxima" función.
- **Automatización**: [COMPROBADO] recordatorio de control, cumpleaños y encuesta de satisfacción.
- **Clínica**: [COMPROBADO] anamnesis, odontograma, fichas de endodoncia, periodoncia, ortodoncia y cirugía; presupuestos con plantillas.
- **Administración**: [COMPROBADO] egresos, recaudación, liquidación por profesional y boletas SII (Chile). Obras sociales y ARCA: No evidenciado.
- **Operación**: roles y permisos [COMPROBADO]; multisucursal [COMERCIAL].
- **Seguridad**: ley chilena de datos "en preparación".
- **Comercial**: prueba de 15 días; precios no publicados.
- **Fuentes**: https://www.dentidesk.com/ · /dentidesk/feature · /dentidesk/solution

### 11. MR Dental Cloud
- **Empresa / país**: MR Desarrollos Inteligentes, México · https://www.mrdentalcloud.com/
- **Agenda**: "agenda inteligente" con límite de dentistas por plan. Detalle: No evidenciado.
- **Turnos digitales**: chatbot de WhatsApp "a las 3 AM", confirmación "1-tap" [COMERCIAL].
- **Clínica**: [COMPROBADO] odontograma exportable a PDF; consentimiento firmado con IP y sello de tiempo.
- **Seguridad / operación**: [COMPROBADO] cifrado AES-256-GCM, bitácora inmutable, roles, exportación HL7 FHIR R4 y JSON, backups diarios. Cumplimiento NOM-004 y NOM-024; "COFEPRIS Autorizado" [COMERCIAL].
- **Comercial**: MXN 350 / 750 por mes; la prueba figura como 7 o 30 días según la sección (contradictorio).
- **Fuentes**: https://www.mrdentalcloud.com/ · /precios · /security-compliance

### 12. SMILE
- **Empresa / país**: SmileApp, LLC (EE. UU.), orientada a LatAm · https://smiledental.app/
- **Agenda**: [COMPROBADO] vista diaria con duraciones de 30 min, 1 h o 1 h 30.
- **Turnos digitales**: [COMPROBADO] WhatsApp con API oficial y confirmación (capturas); reserva por enlace y lista de espera [COMERCIAL].
- **Automatización**: [COMPROBADO] recordatorios, confirmaciones y recall a los 6 meses.
- **Clínica**: [COMPROBADO] odontograma FDI de 5 zonas; periodontograma por voz [COMERCIAL].
- **Administración**: [COMPROBADO] cierre de caja. Mercado Pago y obras sociales: No evidenciado.
- **Operación / seguridad**: sucursales, roles, exportación a Excel o CSV; declara la Ley 25.326 (AR) y leyes de CO, MX, RD y CL.
- **Comercial**: 30 días gratis; USD 19 (Solo) / USD 23 (Clínica) por mes, +USD 15 Pro; mensaje de WhatsApp a USD 0,01.
- **Fuentes**: https://smiledental.app/ · /funciones · /proteccion-de-datos-pacientes · /soporte-por-whatsapp

### 13. Dentiqa
- **Empresa / país**: Violet Wave, Argentina (según la política de privacidad), con foco en LatAm · https://dentiqa.app/
- **Agenda**: [COMPROBADO] vista día y semana, múltiples profesionales, bloqueos por profesional; prevención de doble reserva [COMERCIAL].
- **Turnos digitales**: [COMPROBADO, descripto] chatbot de WhatsApp 24/7 con disponibilidad real; cancelación y reprogramación con validación del nuevo horario.
- **Automatización**: [COMPROBADO] recordatorios a 24 h y 2 h; campañas de reactivación.
- **Clínica**: [COMPROBADO] ficha de 8 pestañas, odontograma versionado, periodontograma, radiografías.
- **Operación**: roles, audit logs, multiclínica (Enterprise).
- **Comercial**: USD 89 / 149 / 249 por mes; sin prueba gratuita evidenciada; "en lanzamiento" con cupo de fundadores.
- **Fuentes**: https://dentiqa.app/ · /software-dental · /legal/politica-de-privacidad

### 14. OdontoPlus
- **Empresa / país**: Kodea Labs / MangoPOS; país: No evidenciado · https://miodontoplus.com/
- **Agenda**: [COMPROBADO] por profesional, sillón o sucursal.
- **Turnos digitales**: [COMPROBADO] recordatorios de WhatsApp desde los que el paciente confirma, cancela o reagenda; **lista de espera que rellena huecos por cancelación**; link de citas para Instagram y Google [COMERCIAL].
- **Clínica**: [COMPROBADO] odontograma por superficies enlazado al presupuesto, plan por fases, RX.
- **Administración**: [COMPROBADO] caja, pagos parciales, facturas y pago de aseguradora.
- **Comercial**: prueba de 14 días; USD 29 / 59 / 119 por mes.
- **Fuentes**: https://miodontoplus.com/ (página única)

### 15. Odontosist
- **Empresa / país**: titular persona física, Lima, Perú · https://odontosist.com/
- **Agenda**: [COMPROBADO] calendario semanal; **al elegir el dentista se bloquean sus horarios ocupados**; bloques de descanso.
- **Turnos digitales**: [COMPROBADO] página pública /reservar/[slug] compartible por WhatsApp y redes; estados Pendiente, Confirmada y Cancelada.
- **Automatización**: [COMPROBADO] recordatorios por WhatsApp y correo a 24 h y 1 h; estado "No se presentó".
- **Clínica**: [COMPROBADO] odontograma FDI de 32 piezas con superficies; planes con sesiones.
- **Integraciones**: Google Calendar [COMPROBADO].
- **Comercial**: prueba de 14 días; S/ 55 / 99 / 350 por mes.
- **Fuentes**: https://odontosist.com/ · /guia-usuario.html

---

## Internacionales (referencia)

### 16. Clinic Cloud
- **Empresa / país**: Doctoralia España SL, Barcelona · https://clinic-cloud.com/
- **Agenda**: multiprofesional y multisede [COMERCIAL]; agendas por plan, +10 EUR por agenda extra.
- **Turnos digitales**: reserva 24/7 vía Doctoralia; confirmaciones por WhatsApp, SMS y email [COMERCIAL].
- **Clínica**: [COMPROBADO] historia personalizable, odontograma, periodontograma (plan Max).
- **Comercial**: EUR 29 / 49 / 79 por mes + IVA.
- **Fuentes**: https://clinic-cloud.com/ · /tarifas · /historias-clinicas-electronicas-digitales-software-gestion/

### 17. Dentally
- **Empresa / país**: Henry Schein One UK Ltd, Reino Unido · https://dentally.com/
- **Agenda**: [COMPROBADO] gestión de huecos de último momento; precio por cantidad de sillones ("surgeries").
- **Turnos digitales**: [COMPROBADO] reserva online desde el portal del paciente; lista de espera [COMERCIAL].
- **Automatización**: [COMPROBADO] recordatorios por SMS y email, recalls inteligentes.
- **Comercial**: GBP 134 a 1.021 por mes según plan y cantidad de sillones.
- **Fuentes**: https://dentally.com/ · /en-gb/pricing · /en-gb/dental-software-features · /en-gb/dental-software-features/practice-admin

### 18. Open Dental
- **Empresa / país**: Open Dental Software, EE. UU. · https://www.opendental.com/
- **Modalidad**: autoalojado o nube opcional; apps ODMobile y ODTouch.
- **Turnos digitales**: [COMPROBADO] Web Sched (recall, paciente nuevo, paciente existente) con alta automática en la agenda; **lista ASAP con enlace para aceptar un hueco liberado**.
- **Automatización**: eReminders, eConfirmations (USD 25/mes por sede).
- **Comercial**: USD 199/mes por sede con contrato de 12 meses; USD 89 en otros países.
- **Fuentes**: https://www.opendental.com/ · /site/fees.html · /site/order.html · /site/documentation.html · /site/websched.html

### 19. Curve Dental
- **Empresa / país**: CD Newco, LLC, Alpharetta, EE. UU. · https://www.curvedental.com/
- **Agenda**: [COMPROBADO] vista por operatorio (sillón) o por profesional, intervalos configurables, bloqueo rápido, arrastrar y soltar; Smart Fill para huecos de último momento.
- **Turnos digitales / automatización**: [COMPROBADO] autoagendado, mensajería bidireccional, recordatorios, campañas.
- **Clínica**: [COMPROBADO] periodontograma gráfico, formularios digitales, imágenes en la nube, planes con firma.
- **Comercial**: precios no publicados.
- **Adopción**: "+100.000 usuarios", 4,6/5 [COMERCIAL].
- **Fuentes**: https://www.curvedental.com/ · /pricing · /dental-scheduling-software · /dental-patient-engagement-software · /feature-overview
