# utils_config.py
class Config:
    URLREGEX = r"^(https?|ftp)://[^\s/$.?#].[^\s]*$"
    URLREGEX_NOT_ALONE = r"http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+"
    FLASH_LINKED_CONTENT = r"http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F])+).*\.swf"
    HREFREGEX = '<a\s*href=[\'|"](.*?)[\'"].*?\s*>'
    IPREGEX = r"\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))\b"
    EMAILREGEX = r"([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+)"

    spam_words = [
        # Existing words
        'claim', 'congratulations', 'free', 'money', 'offer', 'click', 'save',
        'winner', 'guarantee', 'risk-free', 'important', 'alert', 'verify',
        'unauthorized', 'success', 'fantastic', 'incredible', 'cheap', 'prize',
        'limited', 'exclusive', 'subscribe', 'earn', 'discount', 'credit',
        'loans', 'investment', 'trial', 'selected', 'top', 'giveaway', 'jackpot',
        'deal', 'gift', 'bonus', 'fortune', 'voucher', 'rich', 'billion', 'million',
        'cash', 'funds', 'password', 'account', 'username', 'login', 'log-in', 'verify',
        'verification',
        'credentials', 'update', 'confirm', 'confirmation', 'security', 'secure',
        'activity', 'alert', 'notice', 'access', 'recover', 'unlock', 'suspended',
        'deactivation', 'reactivation', 'disabled', 'fraud', 'policy', 'violation',
        'warning', 'account', 'issue', 'risk', 'holder', 'customer', 'client', 'service',
        'payment', 'invoice', 'billing', 'order', 'transaction', 'transfer', 'funds transfer',
        'bank', 'tax', 'refund', 'rebate', 'compensation', 'owe', 'debt', 'credit',
        'score', 'report', 'prizes', 'lottery', 'sweepstakes', 'winner', 'selected',
        'chosen', 'lucky', 'fortunate', 'opportunity', 'chance', 'offer', 'entry',
        'apply', 'terms', 'conditions', 'rates', 'quota', 'quota', 'beneficiary',
        'grant', 'donation', 'charity', 'contribution', 'pledge',
    ]

    stop_words = [
        'the', 'in', 'of', 'to', 'and', 'a', 'is', 'it', 'you', 'that',
        'an', 'this', 'for', 'with', 'on', 'by', 'or', 'be', 'as', 'at'
    ]

    urgency_phrases = [
        'act now', 'act immediately', 'act fast', 'act quick',
        'limited time',
        'immediately',
        'urgent',
        'hurry', 'hurry up',
        'quick', 'quickly',
        'fast',
        'now', 'right now',
        'expires',
        'instant', 'instantly',
        'don\'t miss', 'do not miss',
        'last chance',
        'today only',
        'rush',
        'offer ends',
        'expiring soon', 'expires soon',
        'right away',
        'final call',
        'last day',
        'last minute',
        'act quickly',
        'limited offer',
        'exclusive deal',
        'special offer',
        'special deal',
        'limited edition',
        'only a few left',
        'only available', 'available now',
        'order now',
        'apply today',
        'subscribe now',
        'register now',
        'limited spots',
        'limited availability',
        'while supplies last',
        'closing soon',
        'final opportunity',
        'last opportunity',
        'don\'t wait', 'do not wait',
        'act before',
        'expire soon',
        'approaching deadline',
        'urgent requirement'
    ]
