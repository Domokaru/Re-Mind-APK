package com.hallauer.remind.core.constants

import com.hallauer.remind.data.local.entity.GroupBucket

data class SeedPhrase(
    val bucket: GroupBucket,
    val text: String
)

object SeedPhrases {
    val ALL_50_PHRASES: List<SeedPhrase> = listOf(
        // ==================== BUCKET 1: SPOUSE (10 Phrases - "Love" Permitted) ====================
        SeedPhrase(
            bucket = GroupBucket.SPOUSE,
            text = "Hey. Just taking a breath in the middle of the day to remind you how grateful I am that we are in this together. Love you."
        ),
        SeedPhrase(
            bucket = GroupBucket.SPOUSE,
            text = "Life gets loud, so I just wanted to cut through the noise today and tell you I love you and I am always in your corner."
        ),
        SeedPhrase(
            bucket = GroupBucket.SPOUSE,
            text = "No agenda and nothing needed on your end. Just wanted you to know I am thinking about you today and I love building this life with you."
        ),
        SeedPhrase(
            bucket = GroupBucket.SPOUSE,
            text = "Hope your day is going smoothly. Thank you for everything you carry for our family. I see it, I appreciate you, and I love you."
        ),
        SeedPhrase(
            bucket = GroupBucket.SPOUSE,
            text = "Just a quick pause in my day to tell you that you are my favorite person to walk through life with. Love you."
        ),
        SeedPhrase(
            bucket = GroupBucket.SPOUSE,
            text = "Whatever bricks you are carrying in your backpack today, remember you do not have to carry them alone. I am right here with you. Love you."
        ),
        SeedPhrase(
            bucket = GroupBucket.SPOUSE,
            text = "Thinking of you right now and feeling really thankful to have you by my side. Hope you get a quiet moment for yourself today. Love you."
        ),
        SeedPhrase(
            bucket = GroupBucket.SPOUSE,
            text = "Just checking in from my end. I am proud of you, I am grateful for you, and I love you very much."
        ),
        SeedPhrase(
            bucket = GroupBucket.SPOUSE,
            text = "You make our home a safe, steady place to land every single day. Thank you for being you. Love you."
        ),
        SeedPhrase(
            bucket = GroupBucket.SPOUSE,
            text = "Quick reminder in the middle of the week: you matter to me more than any to-do list. Love you and can't wait to see you later."
        ),

        // ==================== BUCKET 2: FAMILY (10 Phrases - "Love" Permitted) ====================
        SeedPhrase(
            bucket = GroupBucket.FAMILY,
            text = "Thinking about you today. No agenda and no need to text back right away, just wanted you to know I love you and appreciate having you in my life."
        ),
        SeedPhrase(
            bucket = GroupBucket.FAMILY,
            text = "Just checking in to see how your week is going. Grateful for you, and hope you are taking care of yourself today."
        ),
        SeedPhrase(
            bucket = GroupBucket.FAMILY,
            text = "Life moves fast and it is easy to let weeks slip by without saying it, so I just wanted to reach out and say I love you and I'm thankful for you."
        ),
        SeedPhrase(
            bucket = GroupBucket.FAMILY,
            text = "Hey! You crossed my mind today. Hope things are going well on your end and that you have some good things lined up this week."
        ),
        SeedPhrase(
            bucket = GroupBucket.FAMILY,
            text = "Just dropping a quick note to let you know I am in your corner. Hope your day is treating you well. Love you."
        ),
        SeedPhrase(
            bucket = GroupBucket.FAMILY,
            text = "No need for a long reply if you're busy. Just wanted to send a quick reminder that I'm thinking of you and grateful we're family."
        ),
        SeedPhrase(
            bucket = GroupBucket.FAMILY,
            text = "Hope you're catching a breath this week and that life is treating you well. Thinking about you today and sending love your way."
        ),
        SeedPhrase(
            bucket = GroupBucket.FAMILY,
            text = "Wanted to pause today and say thank you for always being a steady part of my life. Hope you're having a great week."
        ),
        SeedPhrase(
            bucket = GroupBucket.FAMILY,
            text = "Checking in on you today. Whether this week has been light or heavy, just know I'm thinking of you and I love you."
        ),
        SeedPhrase(
            bucket = GroupBucket.FAMILY,
            text = "Hey there, just wanted to reach out and say hello. Hope everything is going smoothly on your end this week!"
        ),

        // ==================== BUCKET 3: FRIENDS (10 Phrases - STRICT NO-LOVE GUARDRAIL) ====================
        SeedPhrase(
            bucket = GroupBucket.FRIENDS,
            text = "Hey, you crossed my mind today. I know life stays busy and heavy, so no need for a long reply, just wanted you to know I'm in your corner."
        ),
        SeedPhrase(
            bucket = GroupBucket.FRIENDS,
            text = "Random check-in: grateful for your friendship and hope you're catching a breath this week. Let's catch up soon."
        ),
        SeedPhrase(
            bucket = GroupBucket.FRIENDS,
            text = "Just pausing during a busy week to let you know I appreciate having you as a friend. Hope things are going well on your end."
        ),
        SeedPhrase(
            bucket = GroupBucket.FRIENDS,
            text = "No agenda here, just realized it had been a minute and wanted to say hey. Hope your week is off to a solid start."
        ),
        SeedPhrase(
            bucket = GroupBucket.FRIENDS,
            text = "Thinking about you today. If your backpack is feeling heavy this week, just know you've got people in your corner."
        ),
        SeedPhrase(
            bucket = GroupBucket.FRIENDS,
            text = "Hey! Hope work and life are treating you right this week. Grateful for your friendship and hope all is well."
        ),
        SeedPhrase(
            bucket = GroupBucket.FRIENDS,
            text = "Just a quick heads-up that you're appreciated. No need to text back if your hands are full today, just wanted to check in."
        ),
        SeedPhrase(
            bucket = GroupBucket.FRIENDS,
            text = "Hope you're finding a little margin for yourself this week. Glad to have you in my circle and hope things are going great."
        ),
        SeedPhrase(
            bucket = GroupBucket.FRIENDS,
            text = "Dropping a quick note to check in on you. Hope you and yours are doing well and having a steady week."
        ),
        SeedPhrase(
            bucket = GroupBucket.FRIENDS,
            text = "Hey my friend, just wanted to reach out and say hello today. Hope the week is treating you well!"
        ),

        // ==================== BUCKET 4: ACQUAINTANCES (10 Phrases - STRICT NO-LOVE GUARDRAIL) ====================
        SeedPhrase(
            bucket = GroupBucket.ACQUAINTANCES,
            text = "Hey! Hope your week is going well and that things are treating you right. Just wanted to say hello."
        ),
        SeedPhrase(
            bucket = GroupBucket.ACQUAINTANCES,
            text = "Crossing my mind today and wanted to send a quick note to hope your week is off to a solid start."
        ),
        SeedPhrase(
            bucket = GroupBucket.ACQUAINTANCES,
            text = "Just dropping a quick hello to check in and hope everything is going smoothly on your end."
        ),
        SeedPhrase(
            bucket = GroupBucket.ACQUAINTANCES,
            text = "Hope you're having a productive, steady week. Just wanted to reach out and say hi!"
        ),
        SeedPhrase(
            bucket = GroupBucket.ACQUAINTANCES,
            text = "Hey there, realized it had been a little while and wanted to wish you a great week ahead."
        ),
        SeedPhrase(
            bucket = GroupBucket.ACQUAINTANCES,
            text = "Just a quick check-in to say hello and hope life and work are treating you well."
        ),
        SeedPhrase(
            bucket = GroupBucket.ACQUAINTANCES,
            text = "Thinking of you today and hoping you have a great rest of your week. No rush at all to reply!"
        ),
        SeedPhrase(
            bucket = GroupBucket.ACQUAINTANCES,
            text = "Hope things are going well on your end! Just wanted to send a quick note to say hello."
        ),
        SeedPhrase(
            bucket = GroupBucket.ACQUAINTANCES,
            text = "Wishing you a solid week ahead. Hope all is well with you!"
        ),
        SeedPhrase(
            bucket = GroupBucket.ACQUAINTANCES,
            text = "Hey! Just pausing to send a quick hello and hope everything is going great in your world."
        ),

        // ==================== BUCKET 5: OTHERS (10 Phrases - STRICT NO-LOVE GUARDRAIL) ====================
        SeedPhrase(
            bucket = GroupBucket.OTHERS,
            text = "Hope things are going well on your end. Just dropping a quick note to check in and wish you a great week."
        ),
        SeedPhrase(
            bucket = GroupBucket.OTHERS,
            text = "Thinking of you today and hoping everything is going smoothly on your end."
        ),
        SeedPhrase(
            bucket = GroupBucket.OTHERS,
            text = "Just reaching out with a quick hello. Hope your week is off to a great start."
        ),
        SeedPhrase(
            bucket = GroupBucket.OTHERS,
            text = "Wishing you a steady, productive week ahead. Hope all is well!"
        ),
        SeedPhrase(
            bucket = GroupBucket.OTHERS,
            text = "Just a quick note to say hello and hope everything is going well with you."
        ),
        SeedPhrase(
            bucket = GroupBucket.OTHERS,
            text = "Hope your day is going well. Just wanted to touch base and wish you a good week."
        ),
        SeedPhrase(
            bucket = GroupBucket.OTHERS,
            text = "Sending a quick check-in your way today. Hope all is well on your end."
        ),
        SeedPhrase(
            bucket = GroupBucket.OTHERS,
            text = "Hope the week is treating you well! Just wanted to drop a quick note and say hello."
        ),
        SeedPhrase(
            bucket = GroupBucket.OTHERS,
            text = "Checking in to wish you a great rest of your week. Hope everything is going smoothly."
        ),
        SeedPhrase(
            bucket = GroupBucket.OTHERS,
            text = "Just a quick hello today to hope all is well in your world."
        ),

        // ==================== BUCKET 6: COLLEAGUES ====================
        SeedPhrase(
            bucket = GroupBucket.COLLEAGUES,
            text = "Hey! Just checking in to see how your week is going with projects. Hope all is smooth on your side."
        ),
        SeedPhrase(
            bucket = GroupBucket.COLLEAGUES,
            text = "Hope your week is off to a productive start! Just touching base to say hello."
        ),
        SeedPhrase(
            bucket = GroupBucket.COLLEAGUES,
            text = "Great collaborating with you recently. Hope you get some downtime to recharge this weekend."
        ),

        // ==================== BUCKET 7: MENTORS ====================
        SeedPhrase(
            bucket = GroupBucket.MENTORS,
            text = "Thinking of your advice recently and wanted to say thank you again for your guidance. Hope all is well!"
        ),
        SeedPhrase(
            bucket = GroupBucket.MENTORS,
            text = "Hope your week is going great! Just wanted to send a quick note of appreciation for your mentorship."
        ),

        // ==================== BUCKET 8: CUSTOM ====================
        SeedPhrase(
            bucket = GroupBucket.CUSTOM,
            text = "Hope you are having a wonderful day! Just thinking of you and sending good thoughts."
        ),
        SeedPhrase(
            bucket = GroupBucket.CUSTOM,
            text = "Just a quick hello to check in and see how life is treating you."
        )
    )
}
