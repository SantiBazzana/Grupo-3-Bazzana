# Informe de Discovery
## Sistemas de gestión de turnos y agenda para consultorios odontológicos en Argentina

**Materia**: Desarrollo de Software, Trabajo Global
**Integrantes** (Grupo 3):
- Bazzana, Santiago (líder del proyecto: fundación, Discovery y change OPSX evaluado)
- De Giuseppe, Joaquín
- Romero, Franco
- Castillo, Franco
- Rocamora, Adriano
- Guiretti, Valentino
**Fecha**: 2026-11-20
**Producto en estudio**: sistema de gestión de turnos y agenda de pacientes para consultorios odontológicos, mercado argentino.

---

## Resumen ejecutivo

**Qué se investigó.** Se relevaron 19 sistemas de gestión odontológica: 8 de Argentina, 7 de Latinoamérica y 4 internacionales usados como referencia. La fuente fueron exclusivamente sus sitios oficiales, consultados el 2026-11-20. Cada afirmación se clasificó como *funcionalidad comprobada* (descripta en concreto, en guía, demo, captura o documentación), *afirmación comercial* (dicha por el proveedor sin demostrarla) o *No evidenciado*. Dos sistemas se descartaron por falta de evidencia legible: Doctoralia Argentina, cuyo sitio no respondió, y Reservo, que se renderiza solo con JavaScript.

**Principales hallazgos.**
1. **El estándar de mercado** es agenda, recordatorios por WhatsApp, odontograma e historia clínica. En Argentina, además, se espera facturación ARCA, Mercado Pago y obras sociales.
2. **La prevención de choques de agenda casi nunca está documentada.** Solo Oclusar (por profesional, a nivel de base de datos), Órbita (por sillón, en un producto que todavía está en preventa) y Odontosist (horarios ocupados del dentista) la muestran en concreto. Ninguno documenta la validación conjunta de profesional y sillón.
3. **Hay opacidad comercial.** 6 de los 19 sistemas no publican precios (Bilog, Dentalink, Órbita, ClinIA, Dentidesk y Curve) y varios muestran cifras de adopción sin respaldo ("+15.000 clientes", "+30.000 clínicas").
4. **La IA conversacional por WhatsApp es la tendencia dominante** (Dentia, Órbita, ClinIA, AgendaPro, Dentalink, Dentiqa), pero la mayoría la ofrece en planes superiores o como adicional pago.
5. **Hay contradicciones visibles** en los sitios de los proveedores: AgendaPro muestra dos precios de entrada distintos, MR Dental Cloud indica 7 y 30 días de prueba, y Odontosist no coincide entre su portada y su guía.

**MVP recomendado.** Un núcleo de agenda para consultorios de 1 a 5 odontólogos con 2 o más sillones:
- crear turnos con duración por prestación y **sin choques por profesional ni por sillón**, con margen de 5 minutos y sobreturno explícito;
- cancelar con 6 horas de anticipación liberando el horario;
- agenda del día por odontólogo;
- link de WhatsApp click-to-chat.

Los recordatorios automáticos, la ficha clínica y odontograma, y los cobros y facturación quedan para la etapa siguiente.

---

## Metodología

- **Búsqueda**: búsquedas web de "software odontológico Argentina", "sistema gestión clínica dental Latinoamérica" y variantes. Se seleccionaron los productos con sitio oficial y foco odontológico o de salud.
- **Relevamiento**: de cada producto se leyeron entre 1 y 5 páginas del dominio oficial (inicio, precios, funcionalidades, guía o ayuda, privacidad). Se extrajo cada campo de la consigna y se registró la URL de origen. Las fichas completas están en `docs/discovery/sources/fichas-competidores.md`.
- **Regla de evidencia**: si un dato no figura en las páginas leídas, se consigna **"No evidenciado"**, sin deducirlo. "No evidenciado" no significa que la función no exista, sino que no está demostrada públicamente.
- **Limitaciones**: no se probaron los productos ni se pidieron demos (consigna: no se crean cuentas ni se contacta a proveedores). Varios productos tienen toda su información en una sola página (Dentia, OdontoGRAMA, OdontoPlus). Los precios y las funcionalidades pueden cambiar después de la fecha de consulta.

---

## A. Tabla comparativa (19 sistemas, ordenados por relevancia para Argentina)

Referencias: **C** = comprobado · **Com** = afirmación comercial · **NE** = No evidenciado. Todas las fuentes fueron consultadas el **2026-11-20**.

| # | Producto / Proveedor | País · Segmento | Modalidad | Agenda y choques | Reserva online | Recordatorios | Clínica | Admin / cobros | Integraciones locales | Precio publicado | Fuente |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | **Dentia** / Dentia | AR · consultorio y clínica | Web/PWA | C: calendario por profesional, sobreturnos. Choques: NE | C: bot de IA en WhatsApp que agenda, reprograma y cancela | C: email (Básico), WhatsApp (Pro) | C (descripto): HC, odontograma, periodontograma, presupuestos | C: caja, ARCA A/B/C, Mercado Pago, obras sociales, seña | WhatsApp, ARCA, Mercado Pago, Google Calendar | Sí: ARS 20.000/30.000/40.000 al mes; 30 días gratis | https://dentia.tech/ |
| 2 | **Órbita** / Órbita Global | AR · consultorio, clínica, red | SaaS + app de chat iOS | Com (producto en preventa): **agenda por sillón que impide el choque** | C: chat de IA en WhatsApp 24 h | C: 48 h y 24 h con botones | C: odontograma por voz; periodontograma, HC | Caja por sede, ARCA, Mercado Pago, obras sociales | API oficial de WhatsApp, ARCA | No (a cotizar) | https://hiorbita.com/erp |
| 3 | **Oclusar** / NE | AR · consultorio y clínica | Web y celular | C: día/semana, bloqueos, sobreturno; **la base de datos impide el doble turno del profesional** | C: link; el profesional aprueba | C: manuales (click-to-chat) | Com: HC, odontograma, consentimientos | Com: Mercado Pago, ARCA, obras sociales | WhatsApp wa.me, Google Calendar, Mercado Pago, ARCA | No (gratis para empezar; montos NE) | https://www.oclusar.com.ar/guias/agenda-y-recordatorios-por-whatsapp |
| 4 | **OdontoClinIA** / Emprinet | AR · consultorio, clínica, municipio, cadena | Web con instancia propia | Por profesional o sillón (Com). Choques: NE | C (descripto): turnero web e IA en WhatsApp | C (descripto): WhatsApp y correo | C: odontograma FDI versionado (demo) | C (descripto): caja, ARCA, PUCO | ARCA, PUCO, RENAPER, WhatsApp | No (cotización) | https://www.clinia.com.ar/odontologia |
| 5 | **Bilog** / Bilog | AR · consultorio, clínica, obras sociales | Web, app y escritorio | C: vistas, arrastrar, duración variable, sobreturno. Choques: NE | C: online con QR o link | C: WhatsApp, SMS; Com: email (solo lo dice `llms.txt`) | Ficha, odontograma, presupuestos | Caja, liquidación de obras sociales | Firma digital; Mercado Pago y ARCA: NE; sin API pública (declarado en `llms.txt`) | No | https://bilog.com.ar/ |
| 6 | **Dentalink** / Engenis SpA | CL (sitio AR) · individual a multisede | Nube | Com: control de agenda. Choques: NE | C: link 24/7 (captura) | Com: WhatsApp | Com: HC, odontograma, periodontograma | Com: caja, obras sociales | API y firma como adicionales; ARCA: NE | No (cotización) | https://www.softwaredentalink.com/ar/planes |
| 7 | **AgendaPro** / AgendaPro Inc. | NE (landing AR) · salud y estética | Nube + apps | C: por profesional y sillón, duración por práctica. Choques: NE | C: web, Google Maps, redes, IA | C: WhatsApp | C: anamnesis, odontograma, RX, presupuestos | C: caja, **seña con Mercado Pago**; ARCA "próximamente" | Mercado Pago, Google Calendar, WhatsApp | Sí: ARS 9.900–129.900 al mes, IVA incluido | https://agendapro.com/ar/planes |
| 8 | **Odonto.ar** / NE (Córdoba) | AR · individual a clínica de 10 | Web en la nube | C: duración por motivo (demo). Choques: NE | C: link de reserva (demo) | NE | Odontograma con estados (maqueta) | Com: finanzas, cobertura de obra social | Mercado Pago solo para la suscripción | Sí: USD 29/49/99 al mes; 30 días gratis | https://odonto.ar/demo |
| 9 | **Dentiqa** / Violet Wave | AR · clínicas LatAm | SaaS | C: día/semana, bloqueos. Choques: Com | C (descripto): bot de WhatsApp 24/7 | C: 24 h y 2 h | C: odontograma versionado, periodontograma | NE (solo estadísticas) | WhatsApp Cloud API | Sí: USD 89/149/249 al mes; sin prueba | https://dentiqa.app/software-dental |
| 10 | **SMILE** / SmileApp LLC | EE. UU. (LatAm) · individual a multisede | Nube (Google) | C: vista diaria, duraciones. Choques: NE | Com: enlace y lista de espera | C: WhatsApp con API oficial, recall | C: odontograma FDI | C: cierre de caja | WhatsApp API; Mercado Pago y ARCA: NE | Sí: USD 19/23 + USD 15 Pro; 30 días gratis | https://smiledental.app/ |
| 11 | **OdontoGRAMA** / J. Santolo | NE (.com.ar) · consultorio | Nube | Com: agendas personalizables | NE | NE | Com: ficha con imágenes | Com: caja, facturación a obras sociales | NE | Com: "¡GRATIS!" (sin precios) | https://odontograma.com.ar/ |
| 12 | **Odontosist** / persona física | PE · consultorio a cadena | Web | C: semanal; **bloquea los horarios ocupados del dentista** | C: página pública /reservar | C: WhatsApp y correo, 24 h y 1 h | C: odontograma FDI, planes | C: cobros parciales, reportes | Google Calendar | Sí: S/ 55/99/350 al mes; 14 días gratis | https://odontosist.com/guia-usuario.html |
| 13 | **OdontoPlus** / Kodea Labs | NE · individual a multisede | Nube | C: por profesional, sillón o sucursal. Choques: NE | Com: link | C: WhatsApp para confirmar, cancelar o reagendar; **lista de espera** | C: odontograma por superficies + presupuesto | C: caja, facturas, aseguradora | NE | Sí: USD 29/59/119 al mes; 14 días gratis | https://miodontoplus.com/ |
| 14 | **Dentidesk** / NE | CL · clínicas | Web + apps | C: intervalos, feriados bloqueados | C: botón web | C: correo; WhatsApp "próximamente" | C: anamnesis, odontograma, especialidades | C: boletas SII (Chile) | Solo fiscales de otros países | No; 15 días gratis | https://www.dentidesk.com/dentidesk/feature |
| 15 | **MR Dental Cloud** / MR Desarrollos | MX · independiente a grupo | Nube | Com: agenda inteligente | Com: chatbot de WhatsApp | Com: WhatsApp, SMS, email | C: odontograma en PDF | Com: CFDI (México) | HL7 FHIR; AR: NE | Sí: MXN 350/750 al mes | https://www.mrdentalcloud.com/precios |
| 16 | **Clinic Cloud** / Doctoralia España SL | ES · clínicas sanitarias | Nube (AWS) | Com: multiprofesional | Com: vía Doctoralia | C: recordatorios | C: odontograma, periodontograma (plan Max) | Caja, TPV (España) | Doctoralia | Sí: EUR 29/49/79 al mes + IVA | https://clinic-cloud.com/tarifas |
| 17 | **Curve Dental** / CD Newco LLC | EE. UU. · consultorio a DSO | Nube + app | C: **por operatorio o profesional**, bloqueos, Smart Fill | C: autoagendado | C: texto y email, campañas | C: periodontograma, imágenes | C: cobros con tarjeta, seguros | Solo EE. UU. | No | https://www.curvedental.com/dental-scheduling-software |
| 18 | **Dentally** / Henry Schein One UK | UK · independiente a grupo | Nube | C: huecos de último momento | C: portal del paciente | C: SMS, email, recalls | Com: charting | C: claims, depósitos | Solo UK | Sí: GBP 134–1.021 al mes | https://www.dentally.com/en-gb/pricing |
| 19 | **Open Dental** / Open Dental Software | EE. UU. · cualquier tamaño | Local o nube | Bloqueos (vía Web Sched) | C: Web Sched + **lista ASAP** | eReminders, eConfirmations | Com: chart 3D | Portal de pago | Solo EE. UU. | Sí: USD 199/mes por sede (USD 89 fuera de EE. UU.) | https://www.opendental.com/site/fees.html |

**Excluidos por falta de evidencia legible**: Doctoralia Argentina (`pro.doctoralia.com.ar` no resolvió por DNS y `www.doctoralia.com.ar` rechazó la conexión) y Reservo (`reservo.cl` se renderiza con JavaScript y solo se obtuvieron 363 caracteres).

---

## B. Matriz de puntuación (0 a 5)

**Pesos**: Turnos y automatización **25 %** · Funcionalidad odontológica clínica **20 %** · Integraciones locales y WhatsApp **15 %** · Administración, cobros y facturación **15 %** · Experiencia del paciente **10 %** · Seguridad, exportación y trazabilidad **10 %** · Precio y facilidad de adopción **5 %**.

| # | Sistema | Turnos 25 % | Clínica 20 % | Integr. 15 % | Admin 15 % | Paciente 10 % | Seguridad 10 % | Precio 5 % | **Total ponderado** |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Dentia | 4 | 5 | 5 | 5 | 4 | 4 | 5 | **4,55** |
| 2 | Órbita | 5 | 4 | 4 | 5 | 4 | 4 | 1 | **4,25** |
| 3 | AgendaPro | 5 | 4 | 4 | 3 | 5 | 3 | 4 | **4,10** |
| 4 | OdontoClinIA | 4 | 4 | 4 | 4 | 4 | 3 | 1 | **3,75** |
| 5 | Oclusar | 4 | 3 | 4 | 4 | 3 | 4 | 3 | **3,65** |
| 6 | Dentiqa | 5 | 4 | 3 | 1 | 4 | 3 | 2 | **3,45** |
| 7 | Curve Dental | 5 | 4 | 1 | 3 | 4 | 2 | 1 | **3,30** |
| 8 | OdontoPlus | 4 | 4 | 1 | 3 | 3 | 3 | 5 | **3,25** |
| 9 | SMILE | 4 | 3 | 2 | 2 | 3 | 4 | 5 | **3,15** |
| 10 | Odontosist | 5 | 3 | 1 | 2 | 4 | 2 | 5 | **3,15** |
| 11 | Dentalink | 4 | 3 | 2 | 3 | 4 | 3 | 1 | **3,10** |
| 12 | Bilog | 4 | 3 | 2 | 3 | 4 | 2 | 1 | **3,00** |
| 13 | Clinic Cloud | 3 | 4 | 1 | 2 | 4 | 3 | 4 | **2,90** |
| 14 | Dentally | 4 | 3 | 1 | 3 | 4 | 2 | 2 | **2,90** |
| 15 | Open Dental | 4 | 3 | 1 | 3 | 4 | 1 | 2 | **2,80** |
| 16 | MR Dental Cloud | 3 | 3 | 2 | 1 | 3 | 5 | 3 | **2,75** |
| 17 | Odonto.ar | 3 | 3 | 1 | 2 | 3 | 3 | 5 | **2,65** |
| 18 | Dentidesk | 3 | 4 | 1 | 2 | 3 | 2 | 2 | **2,60** |
| 19 | OdontoGRAMA | 1 | 2 | 0 | 2 | 0 | 1 | 4 | **1,25** |

Cálculo: total = Σ(nota × peso) / 100. Ejemplo para Dentia: (4·25 + 5·20 + 5·15 + 5·15 + 4·10 + 4·10 + 5·5) / 100 = 4,55.

**Nota metodológica.**
- **Escala**: 0 = sin evidencia en el criterio; 1 = solo afirmaciones comerciales aisladas; 2 = cobertura parcial con poca evidencia; 3 = cobertura media, con algunos ítems comprobados; 4 = cobertura amplia con varios ítems comprobados; 5 = cobertura amplia y comprobada en casi todos los sub-ítems de la consigna.
- **"No evidenciado"** suma 0 en su sub-ítem. Por eso un producto con poca información pública puntúa bajo aunque la función exista: la matriz mide **evidencia pública**, no calidad real.
- **Afirmaciones comerciales**: cuentan, pero valen menos que las funciones comprobadas.
- **Integraciones locales**: se puntúan por relevancia para Argentina (WhatsApp, Mercado Pago, ARCA, obras sociales). Los productos de Europa o EE. UU. reciben 1 aunque tengan integraciones ricas en su mercado.
- **Precio**: 5 = precios publicados con prueba gratuita; 1 = solo cotización.
- **Sesgo conocido**: Dentia y OdontoPlus se evaluaron sobre una sola página muy completa; su puntaje depende de que esa página sea veraz. Se recomienda validarlo con demo (sección D).

---

## C. Análisis competitivo

### C.1 Funcionalidades estándar de mercado
- Agenda multiprofesional en la nube, accesible desde el celular (19 de 19).
- Recordatorios automáticos, con WhatsApp como canal principal en Latinoamérica (Dentia, Órbita, Bilog, AgendaPro, SMILE, Odontosist, Dentiqa, OdontoPlus).
- Historia clínica digital con odontograma (presente en casi todos los relevados).
- Presupuestos y consentimientos firmados.
- Caja y reportes básicos.
- Prueba gratuita de 14 a 30 días entre los productos que publican precio.

### C.2 Diferenciadores reales (con evidencia)
- **Prevención del choque de agenda a nivel de datos**: Oclusar (la base de datos impide el doble turno del profesional) y Órbita (agenda por sillón que impide el choque; afirmación comercial, porque el producto está en preventa).
- **Integración fiscal y de cobertura argentina**: Dentia (facturas A/B/C), ClinIA (verificación de cobertura por PUCO) y Órbita (liquidaciones de obras sociales).
- **Asistente de IA en WhatsApp con demo visible**: Dentia y Órbita, este último con API oficial de Meta.
- **Lista de espera para cubrir cancelaciones**: OdontoPlus, Open Dental (ASAP) y Curve (Smart Fill).
- **Agenda por sillón u operatorio** como vista de primera clase: Curve, AgendaPro y OdontoPlus.

### C.3 Vacíos frecuentes del mercado argentino
1. **La validación conjunta de profesional y sillón no está documentada en ningún producto.** Cada uno muestra a lo sumo una dimensión. En un consultorio con 2 o más sillones y profesionales que rotan, ese es justamente el caso del doble turno.
2. **Opacidad de precios**: 6 de 19 no publican precios, y los dos líderes por adopción declarada (Bilog y Dentalink) están entre ellos.
3. **Recordatorios que no salen solos** o reservados a planes caros: en Oclusar son manuales; en Dentalink, Dentia y SMILE el WhatsApp aparece en planes superiores o con costo por mensaje.
4. **Normativa argentina declarada de forma inconsistente**: la cumplen de forma explícita Oclusar, Odonto.ar, Dentia, ClinIA y SMILE; Bilog, Órbita y Dentiqa no la declaran.
5. **Reglas de agenda invisibles**: casi nadie documenta márgenes entre turnos, sobreturnos controlados ni la política de cancelación con anticipación mínima.

### C.4 Oportunidades de innovación
- **Agenda con reglas explícitas y verificables**: choque por profesional y por sillón, margen configurable, sobreturno que solo libera al profesional y nunca al sillón, y cancelación con anticipación mínima. Todo especificado y cubierto por tests automáticos, como argumento de confianza.
- **Precio publicado y simple** para consultorios de 1 a 5 profesionales, frente a la cotización opaca de los líderes.
- **WhatsApp sin costo de entrada**: link click-to-chat en la v1 y migración a la API oficial cuando el consultorio lo justifique.
- **Integraciones locales como módulos enchufables** (Mercado Pago, ARCA, obras sociales) detrás de interfaces, para no atar el núcleo de agenda a proveedores externos.

---

## D. Recomendación final

### D.1 Cinco competidores prioritarios para analizar mediante demo
1. **Dentia**: es el de mayor puntaje, pero su evidencia sale de una sola página. Hay que validar la agenda (choques, boxes) y el bot de WhatsApp.
2. **Órbita**: es el único que anuncia una agenda por sillón que impide el choque. El producto está en preventa, así que la demo sirve para comprobar si esa función existe y si también valida por profesional.
3. **Oclusar**: impide el doble turno por profesional a nivel de base de datos. Hay que ver si contempla el sillón y cómo maneja el sobreturno.
4. **AgendaPro**: tiene agenda por sillón con duración por práctica y seña con Mercado Pago. Hay que aclarar los precios contradictorios.
5. **Bilog**: es el líder local por adopción declarada. Hay que ver la agenda real y los precios.

### D.2 Tres productos de referencia para experiencia de usuario
1. **Curve Dental**: agenda por operatorio o profesional, bloqueos rápidos, arrastrar y soltar, Smart Fill.
2. **Odonto.ar**: demo interactiva del flujo de reserva, con duración por motivo de consulta.
3. **Oclusar**: guías claras y estados de turno por color en las vistas de día y semana.

### D.3 MVP sugerido

**Imprescindible (v1):**
- Configuración de profesionales, sillones, prestaciones con duración fija y horario de atención del consultorio.
- Crear turno: fin = inicio + duración; sin choques por profesional ni por sillón; margen de 5 minutos; sobreturno explícito que solo permite el choque del profesional; el paciente no puede tener turnos superpuestos; no se aceptan turnos en el pasado ni fuera del horario.
- Cancelar turno con al menos 6 horas de anticipación, liberando el horario.
- Agenda del día por odontólogo.
- Link de WhatsApp click-to-chat (wa.me) con el mensaje del turno.

**Diferenciador:**
- Reglas de agenda explícitas, especificadas y testeadas (validación conjunta de profesional y sillón).
- Precio publicado y simple para consultorios de 1 a 5 profesionales.

**Etapas posteriores:**
- Próxima etapa: recordatorios y confirmación automáticos por WhatsApp (API oficial); ficha clínica y odontograma; cobros, seña con Mercado Pago y factura ARCA.
- Más adelante: reprogramar turno; bloqueos y horario por profesional; asistencia y ausentes; reserva del paciente por link; lista de espera; obras sociales y prepagas; multisede.

---

## Verificación de fuentes

Cada integrante fue responsable de una fuente (una por tipo). Se abrió la URL el **2026-11-26**, seis días después del relevamiento, y se comparó con lo que afirmaba el informe. Resultado: 2 fuentes coinciden por completo y 3 obligaron a corregir el informe.

| # | Tipo de fuente | URL | Responsable | Qué afirmaba el informe | Qué se encontró (2026-11-26) | Corrección aplicada |
|---|---|---|---|---|---|---|
| 1 | Sitio oficial | https://bilog.com.ar/ | Joaquín De Giuseppe | "+1.500 clínicas" (comercial); recordatorios por WhatsApp, SMS y email; no hay API pública documentada | La página muestra "+1.500 clínicas y consultorios", "Avisos automáticos de turnos por WhatsApp" y el producto "Bilog Turnos SMS". El **email** y la frase "no hay una API pública documentada" **no aparecen en la página visible**: salen de `bilog.com.ar/llms.txt`, un archivo de texto del mismo sitio pensado para agentes de IA. | En la tabla A, el email pasa de comprobado a afirmación comercial y se aclara que la falta de API está declarada en `llms.txt`. |
| 2 | Página de precios | https://agendapro.com/ar/planes | Franco Romero | ARS 9.900 / 19.900 / 39.900 / 129.900 al mes; la portada dice "desde $13.900" (inconsistencia) | Los cuatro precios coinciden y ahora aclaran "IVA incluido" (además hay una promoción de $990 los primeros 3 meses). La portada `agendapro.com/ar` **ya no muestra "desde $13.900"**: la inconsistencia desapareció. | En la tabla A se quita "inconsistente entre páginas" y se agrega "IVA incluido". |
| 3 | Guía / centro de ayuda | https://www.oclusar.com.ar/guias/agenda-y-recordatorios-por-whatsapp | Franco Castillo | La base de datos impide el doble turno del profesional; los recordatorios no salen solos | Coincide: la guía dice que el anti-doble-turno "está garantizado por la base de datos", incluso con dos personas cargando a la vez, y que el recordatorio se abre con click-to-chat y lo envía el consultorio. | Ninguna. |
| 4 | Política de privacidad / seguridad | https://odonto.ar/privacidad | Adriano Rocamora | Declara Ley 25.326 y AAIP; los datos pueden procesarse fuera de Argentina | Coincide: Ley 25.326 en la sección 7, AAIP como autoridad de control en la 14, y en la 10 ("Transferencias internacionales") dice que los datos pueden procesarse en servidores fuera de Argentina (usa Supabase). | Ninguna. |
| 5 | Demo / funcionalidades | https://hiorbita.com/erp | Valentino Guiretti | Agenda por sillón que impide el choque; factura ARCA | Los textos existen ("Agenda por sillón, con choque imposible" y "factura de ARCA"), pero el producto, Órbita Gestión, figura **en preventa** y con "precio a cotizar". No hay forma pública de comprobar que funcione. | La agenda por sillón pasa de **comprobado** a **afirmación comercial** en la tabla A, el resumen ejecutivo, C.2 y D.1. |

**Qué aprendimos de la verificación**: dos de los tres errores no eran datos inventados, sino de **evidencia mal clasificada**. El agente tomó como "comprobado" un texto de marketing de un producto que todavía no se vende (Órbita) y un archivo para agentes de IA que no ve un usuario (Bilog). El tercero fue un dato que cambió en pocos días (AgendaPro). Por eso la fecha de consulta es obligatoria.

---

## Referencias

Todas consultadas el 2026-11-20.

1. Bilog: https://bilog.com.ar/ · https://bilog.com.ar/llms.txt · https://bilog.com.ar/agenda
2. Dentalink: https://www.softwaredentalink.com/ar · https://www.softwaredentalink.com/ar/planes · https://www.softwaredentalink.com/ar/soporte · https://www.softwaredentalink.com/ar/atrae-mas-pacientes/agendamiento-online
3. Oclusar: https://www.oclusar.com.ar/ · https://www.oclusar.com.ar/guias · https://www.oclusar.com.ar/guias/agenda-y-recordatorios-por-whatsapp
4. OdontoGRAMA: https://odontograma.com.ar/
5. Odonto.ar: https://odonto.ar/ · https://odonto.ar/demo · https://odonto.ar/privacidad
6. Dentia: https://dentia.tech/
7. Órbita: https://hiorbita.com/ · https://hiorbita.com/precios · https://hiorbita.com/erp · https://hiorbita.com/consultorios
8. OdontoClinIA: https://www.clinia.com.ar/odontologia · https://www.clinia.com.ar/faq · https://www.clinia.com.ar/seguridad
9. AgendaPro: https://agendapro.com/ar · https://agendapro.com/ar/planes · https://agendapro.com/ar/dental/software-odontologico
10. Dentidesk: https://www.dentidesk.com/ · https://www.dentidesk.com/dentidesk/feature · https://www.dentidesk.com/dentidesk/solution
11. MR Dental Cloud: https://www.mrdentalcloud.com/ · https://www.mrdentalcloud.com/precios · https://www.mrdentalcloud.com/security-compliance
12. SMILE: https://smiledental.app/ · https://smiledental.app/funciones · https://smiledental.app/proteccion-de-datos-pacientes · https://smiledental.app/soporte-por-whatsapp
13. Dentiqa: https://dentiqa.app/ · https://dentiqa.app/software-dental · https://dentiqa.app/legal/politica-de-privacidad
14. OdontoPlus: https://miodontoplus.com/
15. Odontosist: https://odontosist.com/ · https://odontosist.com/guia-usuario.html
16. Clinic Cloud: https://clinic-cloud.com/ · https://clinic-cloud.com/tarifas · https://clinic-cloud.com/historias-clinicas-electronicas-digitales-software-gestion/
17. Dentally: https://dentally.com/ · https://www.dentally.com/en-gb/pricing · https://www.dentally.com/en-gb/dental-software-features · https://www.dentally.com/en-gb/dental-software-features/practice-admin
18. Open Dental: https://www.opendental.com/ · https://www.opendental.com/site/fees.html · https://www.opendental.com/site/order.html · https://www.opendental.com/site/documentation.html · https://www.opendental.com/site/websched.html
19. Curve Dental: https://www.curvedental.com/ · https://www.curvedental.com/pricing · https://www.curvedental.com/dental-scheduling-software · https://www.curvedental.com/dental-patient-engagement-software · https://www.curvedental.com/feature-overview
20. Intentos fallidos: https://pro.doctoralia.com.ar/ (DNS), https://www.doctoralia.com.ar/ (conexión rechazada), https://reservo.cl/ (contenido no legible).
