import { ContactSchedule, MessageLog, Phrase } from '../types/remind';

export const SEED_PHRASES: Omit<Phrase, 'id' | 'createdAtEpochMillis'>[] = [
  // ==================== BUCKET 1: SPOUSE (10 Phrases) ====================
  {
    bucket: 'SPOUSE',
    text: 'Hey. Just taking a breath in the middle of the day to remind you how grateful I am that we are in this together. Love you.',
    isSeed: true,
  },
  {
    bucket: 'SPOUSE',
    text: 'Life gets loud, so I just wanted to cut through the noise today and tell you I love you and I am always in your corner.',
    isSeed: true,
  },
  {
    bucket: 'SPOUSE',
    text: 'No agenda and nothing needed on your end. Just wanted you to know I am thinking about you today and I love building this life with you.',
    isSeed: true,
  },
  {
    bucket: 'SPOUSE',
    text: 'Hope your day is going smoothly. Thank you for everything you carry for our family. I see it, I appreciate you, and I love you.',
    isSeed: true,
  },
  {
    bucket: 'SPOUSE',
    text: 'Just a quick pause in my day to tell you that you are my favorite person to walk through life with. Love you.',
    isSeed: true,
  },
  {
    bucket: 'SPOUSE',
    text: 'Whatever bricks you are carrying in your backpack today, remember you do not have to carry them alone. I am right here with you. Love you.',
    isSeed: true,
  },
  {
    bucket: 'SPOUSE',
    text: 'Thinking of you right now and feeling really thankful to have you by my side. Hope you get a quiet moment for yourself today. Love you.',
    isSeed: true,
  },
  {
    bucket: 'SPOUSE',
    text: 'Just checking in from my end. I am proud of you, I am grateful for you, and I love you very much.',
    isSeed: true,
  },
  {
    bucket: 'SPOUSE',
    text: 'You make our home a safe, steady place to land every single day. Thank you for being you. Love you.',
    isSeed: true,
  },
  {
    bucket: 'SPOUSE',
    text: "Quick reminder in the middle of the week: you matter to me more than any to-do list. Love you and can't wait to see you later.",
    isSeed: true,
  },

  // ==================== BUCKET 2: FAMILY (10 Phrases) ====================
  {
    bucket: 'FAMILY',
    text: 'Thinking about you today. No agenda and no need to text back right away, just wanted you to know I love you and appreciate having you in my life.',
    isSeed: true,
  },
  {
    bucket: 'FAMILY',
    text: 'Just checking in to see how your week is going. Grateful for you, and hope you are taking care of yourself today.',
    isSeed: true,
  },
  {
    bucket: 'FAMILY',
    text: "Life moves fast and it is easy to let weeks slip by without saying it, so I just wanted to reach out and say I love you and I'm thankful for you.",
    isSeed: true,
  },
  {
    bucket: 'FAMILY',
    text: 'Hey! You crossed my mind today. Hope things are going well on your end and that you have some good things lined up this week.',
    isSeed: true,
  },
  {
    bucket: 'FAMILY',
    text: 'Just dropping a quick note to let you know I am in your corner. Hope your day is treating you well. Love you.',
    isSeed: true,
  },
  {
    bucket: 'FAMILY',
    text: "No need for a long reply if you're busy. Just wanted to send a quick reminder that I'm thinking of you and grateful we're family.",
    isSeed: true,
  },
  {
    bucket: 'FAMILY',
    text: "Hope you're catching a breath this week and that life is treating you well. Thinking about you today and sending love your way.",
    isSeed: true,
  },
  {
    bucket: 'FAMILY',
    text: "Wanted to pause today and say thank you for always being a steady part of my life. Hope you're having a great week.",
    isSeed: true,
  },
  {
    bucket: 'FAMILY',
    text: "Checking in on you today. Whether this week has been light or heavy, just know I'm thinking of you and I love you.",
    isSeed: true,
  },
  {
    bucket: 'FAMILY',
    text: 'Hey there, just wanted to reach out and say hello. Hope everything is going smoothly on your end this week!',
    isSeed: true,
  },

  // ==================== BUCKET 3: FRIENDS (10 Phrases - STRICT NO-LOVE) ====================
  {
    bucket: 'FRIENDS',
    text: "Hey, you crossed my mind today. I know life stays busy and heavy, so no need for a long reply, just wanted you to know I'm in your corner.",
    isSeed: true,
  },
  {
    bucket: 'FRIENDS',
    text: "Random check-in: grateful for your friendship and hope you're catching a breath this week. Let's catch up soon.",
    isSeed: true,
  },
  {
    bucket: 'FRIENDS',
    text: 'Just pausing during a busy week to let you know I appreciate having you as a friend. Hope things are going well on your end.',
    isSeed: true,
  },
  {
    bucket: 'FRIENDS',
    text: 'No agenda here, just realized it had been a minute and wanted to say hey. Hope your week is off to a solid start.',
    isSeed: true,
  },
  {
    bucket: 'FRIENDS',
    text: "Thinking about you today. If your backpack is feeling heavy this week, just know you've got people in your corner.",
    isSeed: true,
  },
  {
    bucket: 'FRIENDS',
    text: 'Hey! Hope work and life are treating you right this week. Grateful for your friendship and hope all is well.',
    isSeed: true,
  },
  {
    bucket: 'FRIENDS',
    text: "Just a quick heads-up that you're appreciated. No need to text back if your hands are full today, just wanted to check in.",
    isSeed: true,
  },
  {
    bucket: 'FRIENDS',
    text: "Hope you're finding a little margin for yourself this week. Glad to have you in my circle and hope things are going great.",
    isSeed: true,
  },
  {
    bucket: 'FRIENDS',
    text: 'Dropping a quick note to check in on you. Hope you and yours are doing well and having a steady week.',
    isSeed: true,
  },
  {
    bucket: 'FRIENDS',
    text: 'Hey my friend, just wanted to reach out and say hello today. Hope the week is treating you well!',
    isSeed: true,
  },

  // ==================== BUCKET 4: ACQUAINTANCES (10 Phrases - STRICT NO-LOVE) ====================
  {
    bucket: 'ACQUAINTANCES',
    text: 'Hey! Hope your week is going well and that things are treating you right. Just wanted to say hello.',
    isSeed: true,
  },
  {
    bucket: 'ACQUAINTANCES',
    text: 'Crossing my mind today and wanted to send a quick note to hope your week is off to a solid start.',
    isSeed: true,
  },
  {
    bucket: 'ACQUAINTANCES',
    text: 'Just dropping a quick hello to check in and hope everything is going smoothly on your end.',
    isSeed: true,
  },
  {
    bucket: 'ACQUAINTANCES',
    text: 'Hope you are having a productive, steady week. Just wanted to reach out and say hi!',
    isSeed: true,
  },
  {
    bucket: 'ACQUAINTANCES',
    text: 'Hey there, realized it had been a little while and wanted to wish you a great week ahead.',
    isSeed: true,
  },
  {
    bucket: 'ACQUAINTANCES',
    text: 'Just a quick check-in to say hello and hope life and work are treating you well.',
    isSeed: true,
  },
  {
    bucket: 'ACQUAINTANCES',
    text: 'Thinking of you today and hoping you have a great rest of your week. No rush at all to reply!',
    isSeed: true,
  },
  {
    bucket: 'ACQUAINTANCES',
    text: 'Hope things are going well on your end! Just wanted to send a quick note to say hello.',
    isSeed: true,
  },
  {
    bucket: 'ACQUAINTANCES',
    text: 'Wishing you a solid week ahead. Hope all is well with you!',
    isSeed: true,
  },
  {
    bucket: 'ACQUAINTANCES',
    text: 'Hey! Just pausing to send a quick hello and hope everything is going great in your world.',
    isSeed: true,
  },

  // ==================== BUCKET 5: OTHERS (10 Phrases - STRICT NO-LOVE) ====================
  {
    bucket: 'OTHERS',
    text: 'Hope things are going well on your end. Just dropping a quick note to check in and wish you a great week.',
    isSeed: true,
  },
  {
    bucket: 'OTHERS',
    text: 'Thinking of you today and hoping everything is going smoothly on your end.',
    isSeed: true,
  },
  {
    bucket: 'OTHERS',
    text: 'Just reaching out with a quick hello. Hope your week is off to a great start.',
    isSeed: true,
  },
  {
    bucket: 'OTHERS',
    text: 'Wishing you a steady, productive week ahead. Hope all is well!',
    isSeed: true,
  },
  {
    bucket: 'OTHERS',
    text: 'Just a quick note to say hello and hope everything is going well with you.',
    isSeed: true,
  },
  {
    bucket: 'OTHERS',
    text: 'Hope your day is going well. Just wanted to touch base and wish you a good week.',
    isSeed: true,
  },
  {
    bucket: 'OTHERS',
    text: 'Sending a quick check-in your way today. Hope all is well on your end.',
    isSeed: true,
  },
  {
    bucket: 'OTHERS',
    text: 'Hope the week is treating you well! Just wanted to drop a quick note and say hello.',
    isSeed: true,
  },
  {
    bucket: 'OTHERS',
    text: 'Checking in to wish you a great rest of your week. Hope everything is going smoothly.',
    isSeed: true,
  },
  {
    bucket: 'OTHERS',
    text: 'Just a quick hello today to hope all is well in your world.',
    isSeed: true,
  },
];

export const INITIAL_SAMPLE_CONTACTS: ContactSchedule[] = [];

