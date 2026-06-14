<div align="center">

<h1>Leafit</h1>

<h3>Здоровый образ жизни. Одно приложение. Ничего лишнего.</h3>

<p>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose"/>
  <img src="https://img.shields.io/badge/Material_3_Expressive-1f6f3e?style=flat-square&logo=materialdesign&logoColor=white" alt="Material 3"/>
  <img src="https://img.shields.io/badge/Offline-100%25-10b981?style=flat-square" alt="Offline"/>
</p>

<p>
  Пять аспектов здоровья — шаги, тренировки, питание, вода и сон — в едином пространстве.<br/>
  Всё работает локально. Без регистрации. Без облака. Без рекламы.
</p>

</div>

<br/>

<div align="center">
  <a href="#-возможности">Возможности</a> &nbsp;·&nbsp;
  <a href="#-технологии">Технологии</a> &nbsp;·&nbsp;
  <a href="#-запуск">Запуск</a>
</div>

---

## ✨ Возможности

Подробный тур по каждому модулю приложения.

<br/>

<table>
<tr>
<td width="42%" align="center">
  <img src="docs/screenshots/onboarding.png" width="230" alt="Онбординг"/>
</td>
<td width="58%" valign="center">

### 👤 Онбординг и профиль

Старт за тридцать секунд — без email, паролей и социальных входов. Пользователь создаёт **один локальный профиль** прямо на устройстве.

- Имя, пол, возраст, рост и вес в пошаговом мастере
- Мгновенный расчёт **ИМТ** и **суточной нормы калорий**
- Мягкая палитра и плавные анимации переходов
- Данные сразу питают расчёты на остальных экранах

</td>
</tr>
</table>

<table>
<tr>
<td width="58%" valign="center">

### 🏠 Главный экран

Центральный хаб, который собирает весь день в одном взгляде. **Кольца активности** показывают прогресс по калориям, воде и сну за долю секунды.

- Шаги, калории, вода и последний сон — на одном экране
- Виджет воды с добавлением и отменой порции в одно касание
- Недельная активность в виде столбиков по дням
- Открытые достижения прямо на дашборде

</td>
<td width="42%" align="center">
  <img src="docs/screenshots/home.png" width="230" alt="Главный экран"/>
</td>
</tr>
</table>

<table>
<tr>
<td width="42%" align="center">
  <img src="docs/screenshots/workout.png" width="230" alt="Тренировка"/>
</td>
<td width="58%" valign="center">

### 🏃 Тренировки

Полноценный режим тренировки с управлением в реальном времени. Шаги во время занятия считаются **отдельно** от дневного счётчика.

- Таймер: старт, пауза, продолжение, завершение
- Живой подсчёт шагов, дистанции и сожжённых калорий
- Заметка к тренировке и сохранение результата
- История за любую дату — без подтверждённых записей нет мусора

</td>
</tr>
</table>

<table>
<tr>
<td width="58%" valign="center">

### 🍽️ Питание

Прозрачный дневник питания, который превращает подсчёт калорий в пару касаний. Никаких онлайн-баз — только то, что ввёл сам.

- Типы приёма пищи: завтрак, обед, ужин, перекус
- Учёт калорий, белков, жиров и углеводов
- Дневной итог в сравнении с персональной нормой
- Мгновенный пересчёт остатка калорий по «принципу светофора»

</td>
<td width="42%" align="center">
  <img src="docs/screenshots/nutrition.png" width="230" alt="Питание"/>
</td>
</tr>
</table>

<table>
<tr>
<td width="42%" align="center">
  <img src="docs/screenshots/sleep.png" width="230" alt="Сон"/>
</td>
<td width="58%" valign="center">

### 😴 Сон

Ручной журнал отдыха с акцентом на простоту ввода и наглядную аналитику недели.

- Время засыпания и пробуждения
- Оценка качества сна по пятибалльной шкале
- Теги и текстовая заметка к каждой записи
- Недельный график и последний сон на дашборде

</td>
</tr>
</table>

<table>
<tr>
<td width="58%" valign="center">

### 💧 Вода

Самый быстрый модуль в приложении — заточен под ввод «на ходу».

- Добавление стандартной порции в одно касание
- Сумма за день и цель гидратации
- Удаление последней ошибочной записи
- **Локальные напоминания** каждые ~2 часа через AlarmManager

</td>
<td width="42%" align="center">
  <img src="docs/screenshots/settings.png" width="230" alt="Настройки и напоминания"/>
</td>
</tr>
</table>

<table>
<tr>
<td width="42%" align="center">
  <img src="docs/screenshots/achievements.png" width="230" alt="Достижения"/>
</td>
<td width="58%" valign="center">

### 🏆 Достижения

Лёгкая игровая механика, которая поддерживает регулярность без давления и рейтингов.

- Набор бейджей: первая тренировка, водная неделя, первый сон и другие
- Разблокировка за реальные действия пользователя
- Карточки меняют вид: активные и приглушённые
- Прогресс виден сразу при открытии приложения

</td>
</tr>
</table>

<table>
<tr>
<td width="58%" valign="center">

### 🙋 Профиль и настройки

Центр персонализации и управления приложением. Изменил параметры — все показатели пересчитались автоматически.

- Редактирование имени, пола, возраста, роста, веса и аватара
- Авторасчёт ИМТ, категории веса и нормы калорий
- Тема: светлая, тёмная или системная
- Переключатель напоминаний и полный сброс данных

</td>
<td width="42%" align="center">
  <img src="docs/screenshots/profile.png" width="230" alt="Профиль"/>
</td>
</tr>
</table>

---

## 🛠️ Технологии

<div align="center">

<img src="https://skillicons.dev/icons?i=kotlin,androidstudio,android,gradle,git&theme=light" alt="Stack"/>

</div>

<br/>

<table>
<tr><td><b>Язык</b></td><td>Kotlin</td></tr>
<tr><td><b>UI</b></td><td>Jetpack Compose · Material Design 3 Expressive</td></tr>
<tr><td><b>Архитектура</b></td><td>MVVM</td></tr>
<tr><td><b>Данные</b></td><td>Room · Flow · StateFlow</td></tr>
<tr><td><b>Фон</b></td><td>AlarmManager · Foreground Service</td></tr>
</table>

---

## 🚀 Запуск

```bash
git clone https://github.com/<USER>/Leafit.git
```

Открой в **Android Studio**, дождись синхронизации Gradle и запусти на устройстве с **Android 8.0+**.

<sub>Разрешения: <code>ACTIVITY_RECOGNITION</code> для шагов, <code>POST_NOTIFICATIONS</code> для напоминаний.</sub>

---

<div align="center">

<br/>

<img src="docs/logo.png" width="40" alt=""/>
